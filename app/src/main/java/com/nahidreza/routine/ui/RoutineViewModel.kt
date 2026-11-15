package com.nahidreza.routine.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nahidreza.routine.data.DecodedBackup
import com.nahidreza.routine.data.ResolvedEntry
import com.nahidreza.routine.data.RoutineRepository
import com.nahidreza.routine.data.RoutineScheduleEngine
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.CancelledInstance
import com.nahidreza.routine.model.CtCourseRecord
import com.nahidreza.routine.model.HolidayRange
import com.nahidreza.routine.model.RepeatPattern
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.RoutineEntry
import com.nahidreza.routine.model.Subject
import com.nahidreza.routine.notification.AlarmScheduler
import com.nahidreza.routine.notification.NotificationHelper
import com.nahidreza.routine.util.RoutineImageExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

sealed interface RoutineRoute {
    data object Schedule : RoutineRoute
    data class AddEditClass(
        val editEntryId: String? = null,
        val prefillDay: Int? = null,
        val prefillSlot: Int? = null,
        val targetParity: Int = RepeatPattern.EVERY_WEEK
    ) : RoutineRoute

    data object CtTracker : RoutineRoute
    data object Settings : RoutineRoute
    data object ManageSubjects : RoutineRoute
    data class EditSubject(val subjectId: String? = null) : RoutineRoute
    data object ManageHolidays : RoutineRoute
}

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineRepository(application)

    val dbState: StateFlow<AppDatabaseState> = repository.stateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoutineRepository.createEmptyState()
        )

    private val _selectedDate = MutableStateFlow(RoutineDate.nearestActiveOrNextWorkingDate())
    val selectedDate: StateFlow<RoutineDate> = _selectedDate.asStateFlow()

    @Volatile
    private var hasAppliedInitialUpNextFocus = false

    @Volatile
    private var hasExplicitDateSelection = false

    private val _currentMinutesOfDay = MutableStateFlow(RoutineScheduleEngine.currentMinutesOfDay())
    val currentMinutesOfDay: StateFlow<Int> = _currentMinutesOfDay.asStateFlow()

    private val _route = MutableStateFlow<RoutineRoute>(RoutineRoute.Schedule)
    val route: StateFlow<RoutineRoute> = _route.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        NotificationHelper.ensureChannel(application)
        viewModelScope.launch(Dispatchers.Default) {
            repository.ensureInitialized()
            repository.stateFlow.collect { state ->
                if (!hasAppliedInitialUpNextFocus) {
                    hasAppliedInitialUpNextFocus = true
                    if (!hasExplicitDateSelection) {
                        val nowMinutes = RoutineScheduleEngine.currentMinutesOfDay()
                        _currentMinutesOfDay.value = nowMinutes
                        val upNext = RoutineScheduleEngine.findNextClassTarget(
                            state = state,
                            nowDate = RoutineDate.today(),
                            nowMinutesOfDay = nowMinutes
                        )
                        if (upNext != null) {
                            _selectedDate.value = upNext.first
                        }
                    }
                }
                AlarmScheduler.scheduleUpcomingAlarms(getApplication(), state)
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                _currentMinutesOfDay.value = RoutineScheduleEngine.currentMinutesOfDay()
            }
        }
    }

    fun navigateTo(target: RoutineRoute) {
        _route.value = target
    }

    fun navigateBackToSchedule() {
        _route.value = RoutineRoute.Schedule
    }

    fun selectDate(date: RoutineDate) {
        if (date != _selectedDate.value) {
            hasExplicitDateSelection = true
        }
        _selectedDate.value = date
    }

    fun selectDateFromNotification(date: RoutineDate) {
        hasExplicitDateSelection = true
        _selectedDate.value = date
    }

    fun setViewMode(mode: String) {
        viewModelScope.launch {
            repository.updateState { it.copy(settings = it.settings.copy(viewMode = mode)) }
        }
    }

    fun jumpToUpNext(): RoutineDate? {
        val state = dbState.value
        val target = RoutineScheduleEngine.findNextClassTarget(
            state = state,
            nowDate = RoutineDate.today(),
            nowMinutesOfDay = _currentMinutesOfDay.value
        )
        return if (target != null) {
            _selectedDate.value = target.first
            target.first
        } else {
            _toastMessage.tryEmit("No upcoming classes scheduled")
            null
        }
    }

    fun toggleCancelForDate(entryId: String, date: RoutineDate) {
        val dateIso = date.toIso()
        viewModelScope.launch {
            var isNowCancelled = false
            repository.updateState { current ->
                val exists = current.cancellations.any { it.entryId == entryId && it.dateIso == dateIso }
                isNowCancelled = !exists
                val updatedList = if (exists) {
                    current.cancellations.filterNot { it.entryId == entryId && it.dateIso == dateIso }
                } else {
                    current.cancellations + CancelledInstance(entryId, dateIso)
                }
                current.copy(cancellations = updatedList)
            }
            _toastMessage.tryEmit(if (isNowCancelled) "Class cancelled for ${date.shortDayName}" else "Class restored")
        }
    }

    fun shiftLabNextWeek(entryId: String, currentAcademicWeek: Int) {
        viewModelScope.launch {
            var didShift = false
            var targetFound = false
            repository.updateState { current ->
                val target = current.entries.firstOrNull { it.id == entryId } ?: return@updateState current
                targetFound = true
                if (!RoutineScheduleEngine.canShiftLabNextWeek(current.entries, target)) {
                    return@updateState current
                }
                val newParity = if (target.repeatPattern == RepeatPattern.ODD_WEEKS) {
                    RepeatPattern.EVEN_WEEKS
                } else {
                    RepeatPattern.ODD_WEEKS
                }
                val preservedHistory = target.parityShiftHistory.toMutableMap()
                for (w in 1 until currentAcademicWeek) {
                    if (!preservedHistory.containsKey(w)) {
                        preservedHistory[w] = target.parityForAcademicWeek(w)
                    }
                }
                val updatedEntry = target.copy(
                    repeatPattern = newParity,
                    parityShiftHistory = preservedHistory
                )
                didShift = true
                current.copy(
                    entries = current.entries.map { if (it.id == entryId) updatedEntry else it }
                )
            }
            if (didShift) {
                _selectedDate.value = _selectedDate.value.plusDays(7)
                _toastMessage.tryEmit("Lab shifted to next week's cycle")
            } else if (targetFound) {
                _toastMessage.tryEmit("Cannot shift: slot occupied next week")
            }
        }
    }

    fun swapTheoryClass(entry: RoutineEntry, academicWeek: Int, direction: Int) {
        viewModelScope.launch {
            val target = RoutineScheduleEngine.findSwapTarget(
                entries = dbState.value.entries,
                entry = entry,
                academicWeek = academicWeek,
                direction = direction
            ) ?: return@launch

            repository.updateState { current ->
                val e1 = current.entries.firstOrNull { it.id == entry.id } ?: return@updateState current
                val e2 = current.entries.firstOrNull { it.id == target.id } ?: return@updateState current
                current.copy(
                    entries = current.entries.map { item ->
                        when (item.id) {
                            e1.id -> item.copy(startSlot = e2.startSlot)
                            e2.id -> item.copy(startSlot = e1.startSlot)
                            else -> item
                        }
                    }
                )
            }
            _toastMessage.tryEmit("Swapped slots for every ${RoutineDate.FULL_DAY_NAMES[entry.dayOfWeek]}")
        }
    }

    fun deleteRoutineEntry(entryId: String) {
        viewModelScope.launch {
            repository.updateState { current ->
                current.copy(
                    entries = current.entries.filterNot { it.id == entryId },
                    cancellations = current.cancellations.filterNot { it.entryId == entryId }
                )
            }
            _toastMessage.tryEmit("Removed from routine")
        }
    }

    fun saveRoutineEntry(
        editEntryId: String?,
        subjectId: String,
        isLab: Boolean,
        teacher: String,
        dayOfWeek: Int,
        startSlot: Int,
        repeatPattern: Int
    ): Boolean {
        val state = dbState.value
        val span = if (isLab) 3 else 1
        if (RoutineScheduleEngine.isSlotOccupied(
                entries = state.entries,
                dayOfWeek = dayOfWeek,
                startSlot = startSlot,
                span = span,
                repeatPattern = repeatPattern,
                excludeEntryId = editEntryId
            )
        ) {
            _toastMessage.tryEmit("Selected slot is occupied")
            return false
        }

        viewModelScope.launch {
            repository.updateState { current ->
                val existing = current.entries.firstOrNull { it.id == editEntryId }
                val newEntry = if (existing != null) {
                    existing.copy(
                        subjectId = subjectId,
                        isLab = isLab,
                        teacher = teacher,
                        dayOfWeek = dayOfWeek,
                        startSlot = startSlot,
                        span = span,
                        repeatPattern = repeatPattern
                    )
                } else {
                    RoutineEntry(
                        id = "ent_${UUID.randomUUID().toString().take(8)}",
                        subjectId = subjectId,
                        isLab = isLab,
                        teacher = teacher,
                        dayOfWeek = dayOfWeek,
                        startSlot = startSlot,
                        span = span,
                        repeatPattern = repeatPattern
                    )
                }
                val updatedEntries = if (existing != null) {
                    current.entries.map { if (it.id == existing.id) newEntry else it }
                } else {
                    current.entries + newEntry
                }
                current.copy(entries = updatedEntries)
            }
        }
        _toastMessage.tryEmit(if (editEntryId != null) "Class updated" else "Class added")
        return true
    }

    fun saveSubject(subjectId: String?, code: String, colorId: String, teachers: List<String>) {
        val cleanCode = code.trim()
        if (cleanCode.isEmpty()) return
        viewModelScope.launch {
            repository.updateState { current ->
                val existing = current.subjects.firstOrNull { it.id == subjectId }
                val targetId = existing?.id ?: "sub_${UUID.randomUUID().toString().take(8)}"
                val updatedSubject = Subject(
                    id = targetId,
                    code = cleanCode,
                    colorId = colorId,
                    teachers = teachers.distinct()
                )
                val newSubjects = if (existing != null) {
                    current.subjects.map { if (it.id == existing.id) updatedSubject else it }
                } else {
                    current.subjects + updatedSubject
                }
                val updatedCt = if (!updatedSubject.isEvenCode && current.ctRecords.none { it.subjectId == targetId }) {
                    current.ctRecords + CtCourseRecord(subjectId = targetId)
                } else {
                    current.ctRecords
                }
                current.copy(subjects = newSubjects, ctRecords = updatedCt)
            }
            _toastMessage.tryEmit(if (subjectId != null) "Subject updated" else "Subject added")
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            repository.updateState { current ->
                val removedEntryIds = current.entries
                    .filter { it.subjectId == subjectId }
                    .mapTo(HashSet()) { it.id }
                current.copy(
                    subjects = current.subjects.filterNot { it.id == subjectId },
                    entries = current.entries.filterNot { it.subjectId == subjectId },
                    cancellations = current.cancellations.filterNot { it.entryId in removedEntryIds },
                    ctRecords = current.ctRecords.filterNot { it.subjectId == subjectId }
                )
            }
            _toastMessage.tryEmit("Subject deleted")
        }
    }

    fun updateCtMark(subjectId: String, index: Int, score: Int?) {
        val sanitized = score?.coerceIn(0, 20)
        viewModelScope.launch {
            repository.updateState { current ->
                val existing = current.ctRecords.firstOrNull { it.subjectId == subjectId }
                    ?: CtCourseRecord(subjectId = subjectId)
                val list = existing.marks.toMutableList()
                while (list.size <= index) list.add(null)
                list[index] = sanitized
                val updatedRecord = existing.copy(marks = list)
                val updatedAll = if (current.ctRecords.any { it.subjectId == subjectId }) {
                    current.ctRecords.map { if (it.subjectId == subjectId) updatedRecord else it }
                } else {
                    current.ctRecords + updatedRecord
                }
                current.copy(ctRecords = updatedAll)
            }
        }
    }

    fun updateSemesterStart(dateIso: String) {
        viewModelScope.launch {
            repository.updateState { current ->
                current.copy(settings = current.settings.copy(semesterStartDateIso = dateIso))
            }
        }
    }

    fun adjustManualWeekOffset(delta: Int) {
        viewModelScope.launch {
            repository.updateState { current ->
                current.copy(
                    settings = current.settings.copy(
                        manualWeekOffset = (current.settings.manualWeekOffset + delta).coerceIn(-20, 20)
                    )
                )
            }
        }
    }

    fun addHoliday(name: String, startIso: String, endIso: String) {
        val cleanName = name.trim().ifEmpty { "Holiday" }
        val startDate = RoutineDate.fromIso(startIso)
        val endDate = RoutineDate.fromIso(endIso)
        val (normalizedStart, normalizedEnd) = if (startDate != null && endDate != null && startDate > endDate) {
            endDate.toIso() to startDate.toIso()
        } else {
            startIso to endIso
        }
        viewModelScope.launch {
            repository.updateState { current ->
                current.copy(
                    holidays = current.holidays + HolidayRange(
                        id = "hol_${UUID.randomUUID().toString().take(8)}",
                        name = cleanName,
                        startDateIso = normalizedStart,
                        endDateIso = normalizedEnd
                    )
                )
            }
            _toastMessage.tryEmit("Holiday saved")
        }
    }

    fun deleteHoliday(holidayId: String) {
        viewModelScope.launch {
            repository.updateState { current ->
                current.copy(holidays = current.holidays.filterNot { it.id == holidayId })
            }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateState { current ->
                current.copy(settings = current.settings.copy(notificationsEnabled = enabled))
            }
        }
    }

    fun updateNotificationLeadMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.updateState { current ->
                current.copy(settings = current.settings.copy(notificationLeadMinutes = minutes))
            }
        }
    }

    fun exportBackupJson(scheduleOnly: Boolean = false): String {
        return if (scheduleOnly) {
            repository.exportScheduleOnlyJson(dbState.value)
        } else {
            repository.exportToJson(dbState.value)
        }
    }

    fun importBackupUri(uri: Uri) {
        viewModelScope.launch {
            val raw = runCatching {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                }
            }.getOrNull()

            if (raw.isNullOrBlank()) {
                _toastMessage.tryEmit("Could not read JSON file")
                return@launch
            }
            val parsed = repository.decodeBackup(raw)
            parsed.onSuccess { decoded ->
                when (decoded) {
                    is DecodedBackup.FullBackup -> {
                        repository.saveState(decoded.state)
                        _toastMessage.tryEmit("Full backup restored")
                    }
                    is DecodedBackup.ScheduleOnly -> {
                        repository.updateState { current ->
                            RoutineRepository.mergeSharedSchedule(current, decoded.schedule)
                        }
                        _toastMessage.tryEmit("Schedule imported (CT marks kept)")
                    }
                }
            }.onFailure {
                _toastMessage.tryEmit("Invalid routine JSON file")
            }
        }
    }

    fun exportBackupUri(uri: Uri, scheduleOnly: Boolean = false) {
        viewModelScope.launch {
            val result = runCatching {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(exportBackupJson(scheduleOnly).toByteArray(Charsets.UTF_8))
                }
            }
            val successLabel = if (scheduleOnly) "Schedule JSON saved" else "Full backup JSON saved"
            _toastMessage.tryEmit(if (result.isSuccess) successLabel else "Failed to save JSON")
        }
    }

    fun masterReset() {
        viewModelScope.launch {
            repository.saveState(RoutineRepository.createEmptyState(dbState.value.settings))
            _toastMessage.tryEmit("All routine data cleared")
        }
    }

    fun saveRoutinePng() {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.Default) {
                val bitmap = renderCurrentScheduleBitmap()
                RoutineImageExporter.saveToGallery(getApplication(), bitmap)
            }
            _toastMessage.tryEmit(if (saved) "Saved PNG to Pictures/ROUTINE" else "Could not save image")
        }
    }

    fun shareRoutinePng(launchChooser: (Intent) -> Unit) {
        viewModelScope.launch {
            val intent = withContext(Dispatchers.Default) {
                val bitmap = renderCurrentScheduleBitmap()
                RoutineImageExporter.createShareIntent(getApplication(), bitmap)
            }
            if (intent != null) {
                launchChooser(intent)
            } else {
                _toastMessage.tryEmit("Failed to prepare image for sharing")
            }
        }
    }

    private fun renderCurrentScheduleBitmap(): android.graphics.Bitmap {
        val state = dbState.value
        val date = _selectedDate.value
        return if (state.settings.viewMode == "WEEK") {
            val weekStart = date.startOfUniversityWeek()
            val weekDates = (0..4).map { weekStart.plusDays(it) }
            val academicWeek = RoutineScheduleEngine.academicWeekFor(weekStart, state)
            val weekHoliday = RoutineScheduleEngine.holidayForWeek(weekDates, state.holidays)
            val byDay = weekDates.map { d ->
                RoutineScheduleEngine.entriesForDate(d, state)
            }
            RoutineImageExporter.renderWeekBitmap(
                weekDates = weekDates,
                academicWeek = academicWeek,
                byDay = byDay,
                weekHoliday = weekHoliday
            )
        } else {
            val holiday = RoutineScheduleEngine.isHoliday(date, state.holidays)
            val items: List<ResolvedEntry> = RoutineScheduleEngine.entriesForDate(date, state)
            RoutineImageExporter.renderDayBitmap(
                date = date,
                items = items,
                holiday = holiday
            )
        }
    }
}
