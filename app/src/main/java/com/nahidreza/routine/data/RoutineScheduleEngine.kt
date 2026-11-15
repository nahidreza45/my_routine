package com.nahidreza.routine.data

import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.HolidayRange
import com.nahidreza.routine.model.RepeatPattern
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.RoutineEntry
import com.nahidreza.routine.model.ScheduleSlots
import com.nahidreza.routine.model.Subject
import java.util.Calendar

data class ResolvedEntry(
    val entry: RoutineEntry,
    val subject: Subject,
    val displayCode: String,
    val date: RoutineDate,
    val academicWeek: Int,
    val isCancelled: Boolean,
    val isLiveNow: Boolean,
    val isUpNext: Boolean
)

object RoutineScheduleEngine {

    fun academicWeekFor(date: RoutineDate, state: AppDatabaseState): Int {
        val semStart = RoutineDate.fromIso(state.settings.semesterStartDateIso)
            ?: RoutineDate.today().startOfUniversityWeek()
        val rawWeek = date.academicWeekNumber(semStart, state.settings.manualWeekOffset)
        val semSat = semStart.startOfUniversityWeek()
        val targetSat = date.startOfUniversityWeek()
        val holidayShift = countHolidayWeeksBetween(semSat, targetSat, state.holidays)
        return rawWeek + holidayShift
    }

    private fun countHolidayWeeksBetween(
        semSat: RoutineDate,
        targetSat: RoutineDate,
        holidays: List<HolidayRange>
    ): Int {
        if (holidays.isEmpty() || semSat == targetSat) return 0
        val intervals = holidays.mapNotNull { h ->
            val s = RoutineDate.fromIso(h.startDateIso)?.startOfUniversityWeek() ?: return@mapNotNull null
            val e = RoutineDate.fromIso(h.endDateIso)?.startOfUniversityWeek() ?: return@mapNotNull null
            val idxS = Math.floorDiv(semSat.daysUntil(s), 7L).toInt()
            val idxE = Math.floorDiv(semSat.daysUntil(e), 7L).toInt()
            if (idxE < idxS) idxE..idxS else idxS..idxE
        }.sortedBy { it.first }

        if (intervals.isEmpty()) return 0

        // Merge overlapping holiday week intervals so duplicate/adjacent ranges are never double-counted
        val merged = mutableListOf<IntRange>()
        for (range in intervals) {
            val prev = merged.lastOrNull()
            if (prev == null || range.first > prev.last + 1) {
                merged.add(range)
            } else {
                merged[merged.lastIndex] = prev.first..maxOf(prev.last, range.last)
            }
        }

        val targetIdx = Math.floorDiv(semSat.daysUntil(targetSat), 7L).toInt()
        var count = 0
        return if (targetIdx > 0) {
            for (r in merged) {
                val overlapStart = maxOf(r.first, 0)
                val overlapEnd = minOf(r.last, targetIdx - 1)
                if (overlapEnd >= overlapStart) count += (overlapEnd - overlapStart + 1)
            }
            -count
        } else {
            for (r in merged) {
                val overlapStart = maxOf(r.first, targetIdx)
                val overlapEnd = minOf(r.last, -1)
                if (overlapEnd >= overlapStart) count += (overlapEnd - overlapStart + 1)
            }
            count
        }
    }

    fun isHoliday(date: RoutineDate, holidays: List<HolidayRange>): HolidayRange? {
        return holidays.firstOrNull { it.contains(date) }
    }

    fun holidayForWeek(weekDates: List<RoutineDate>, holidays: List<HolidayRange>): HolidayRange? {
        if (weekDates.isEmpty() || holidays.isEmpty()) return null
        return holidays.firstOrNull { h -> weekDates.any { d -> h.contains(d) } }
    }

    fun entriesForDate(
        date: RoutineDate,
        state: AppDatabaseState,
        liveNowMinutes: Int? = null,
        upNextTarget: Pair<RoutineDate, String>? = null
    ): List<ResolvedEntry> {
        if (!date.isWorkingDay) return emptyList()
        if (isHoliday(date, state.holidays) != null) return emptyList()
        val dayOfWeek = date.satBasedDayOfWeek
        val academicWeek = academicWeekFor(date, state)
        val dateIso = date.toIso()
        val today = RoutineDate.today()
        val isToday = date == today
        val subjectMap = state.subjects.associateBy { it.id }
        val cancelledIds = state.cancellations
            .filter { it.dateIso == dateIso }
            .mapTo(HashSet()) { it.entryId }

        return state.entries
            .filter { it.dayOfWeek == dayOfWeek && it.isActiveInAcademicWeek(academicWeek) }
            .sortedBy { it.startSlot }
            .mapNotNull { entry ->
                val subject = subjectMap[entry.subjectId] ?: return@mapNotNull null
                val cancelled = entry.id in cancelledIds
                val startSpec = ScheduleSlots.slots.getOrNull(entry.startSlot) ?: return@mapNotNull null
                val endSpec = ScheduleSlots.slots.getOrNull(
                    (entry.startSlot + entry.span - 1).coerceIn(0, ScheduleSlots.slots.lastIndex)
                ) ?: startSpec

                val isLive = isToday && !cancelled && liveNowMinutes != null &&
                    liveNowMinutes in startSpec.startMinutesOfDay until endSpec.endMinutesOfDay

                val isUpNext = !cancelled && !isLive && upNextTarget != null &&
                    upNextTarget.first == date && upNextTarget.second == entry.id

                ResolvedEntry(
                    entry = entry,
                    subject = subject,
                    displayCode = subject.displayCodeFor(entry.isLab),
                    date = date,
                    academicWeek = academicWeek,
                    isCancelled = cancelled,
                    isLiveNow = isLive,
                    isUpNext = isUpNext
                )
            }
    }

    fun findNextClassTarget(
        state: AppDatabaseState,
        nowDate: RoutineDate = RoutineDate.today(),
        nowMinutesOfDay: Int = currentMinutesOfDay()
    ): Pair<RoutineDate, String>? {
        val subjectIds = state.subjects.mapTo(HashSet()) { it.id }
        for (offset in 0..21) {
            val candidateDate = nowDate.plusDays(offset)
            if (!candidateDate.isWorkingDay) continue
            if (isHoliday(candidateDate, state.holidays) != null) continue

            val academicWeek = academicWeekFor(candidateDate, state)
            val dateIso = candidateDate.toIso()
            val cancelledIds = state.cancellations
                .filter { it.dateIso == dateIso }
                .mapTo(HashSet()) { it.entryId }

            val dayEntries = state.entries
                .filter {
                    it.dayOfWeek == candidateDate.satBasedDayOfWeek &&
                        it.subjectId in subjectIds &&
                        it.isActiveInAcademicWeek(academicWeek) &&
                        it.id !in cancelledIds
                }
                .sortedBy { it.startSlot }

            for (entry in dayEntries) {
                val startSpec = ScheduleSlots.slots.getOrNull(entry.startSlot) ?: continue
                val endSpec = ScheduleSlots.slots.getOrNull(
                    (entry.startSlot + entry.span - 1).coerceIn(0, ScheduleSlots.slots.lastIndex)
                ) ?: startSpec

                if (offset == 0) {
                    // On today: match either currently ongoing or strictly upcoming
                    if (nowMinutesOfDay < endSpec.endMinutesOfDay) {
                        return candidateDate to entry.id
                    }
                } else {
                    // Future day: first non-cancelled class
                    if (startSpec.startMinutesOfDay >= 0) {
                        return candidateDate to entry.id
                    }
                }
            }
        }
        return null
    }

    fun isSlotOccupied(
        entries: List<RoutineEntry>,
        dayOfWeek: Int,
        startSlot: Int,
        span: Int,
        repeatPattern: Int,
        excludeEntryId: String? = null
    ): Boolean {
        return entries.any { existing ->
            existing.id != excludeEntryId &&
                existing.dayOfWeek == dayOfWeek &&
                existing.overlapsSlotRange(startSlot, span) &&
                RepeatPattern.conflicts(existing.repeatPattern, repeatPattern)
        }
    }

    fun findFirstAvailableSlot(
        entries: List<RoutineEntry>,
        dayOfWeek: Int,
        isLab: Boolean,
        repeatPattern: Int,
        excludeEntryId: String? = null
    ): Int? {
        val candidates = if (isLab) ScheduleSlots.labBlockStartSlots else ScheduleSlots.slots.indices.toList()
        val span = if (isLab) 3 else 1
        return candidates.firstOrNull { candidateSlot ->
            !isSlotOccupied(
                entries = entries,
                dayOfWeek = dayOfWeek,
                startSlot = candidateSlot,
                span = span,
                repeatPattern = repeatPattern,
                excludeEntryId = excludeEntryId
            )
        }
    }

    fun canShiftLabNextWeek(entries: List<RoutineEntry>, entry: RoutineEntry): Boolean {
        if (!entry.isLab || entry.repeatPattern == RepeatPattern.EVERY_WEEK) return false
        val targetParity = if (entry.repeatPattern == RepeatPattern.ODD_WEEKS) {
            RepeatPattern.EVEN_WEEKS
        } else {
            RepeatPattern.ODD_WEEKS
        }
        return !isSlotOccupied(
            entries = entries,
            dayOfWeek = entry.dayOfWeek,
            startSlot = entry.startSlot,
            span = entry.span,
            repeatPattern = targetParity,
            excludeEntryId = entry.id
        )
    }

    fun findSwapTarget(
        entries: List<RoutineEntry>,
        entry: RoutineEntry,
        academicWeek: Int,
        direction: Int // -1 = up (previous), +1 = down (next)
    ): RoutineEntry? {
        if (entry.isLab) return null
        val activeTheoryToday = entries
            .filter {
                !it.isLab &&
                    it.dayOfWeek == entry.dayOfWeek &&
                    it.isActiveInAcademicWeek(academicWeek)
            }
            .sortedBy { it.startSlot }
        val index = activeTheoryToday.indexOfFirst { it.id == entry.id }
        if (index == -1) return null
        return activeTheoryToday.getOrNull(index + direction)
    }

    fun currentMinutesOfDay(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }
}
