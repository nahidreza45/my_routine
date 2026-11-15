package com.nahidreza.routine.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nahidreza.routine.data.RoutineScheduleEngine
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.ColorPalette
import com.nahidreza.routine.model.RepeatPattern
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.RoutineEntry
import com.nahidreza.routine.model.ScheduleSlots
import com.nahidreza.routine.model.Subject

private enum class AddSheetType {
    SUBJECT, TEACHER, DAY, TIME, REPEAT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassScreen(
    dbState: AppDatabaseState,
    editEntryId: String?,
    prefillDay: Int?,
    prefillSlot: Int?,
    initialParity: Int,
    onSave: (
        editEntryId: String?,
        subjectId: String,
        isLab: Boolean,
        teacher: String,
        dayOfWeek: Int,
        startSlot: Int,
        repeatPattern: Int
    ) -> Boolean,
    onManageSubjects: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val editingEntry = remember(editEntryId, dbState.entries) {
        dbState.entries.firstOrNull { it.id == editEntryId }
    }

    var selectedSubjectId by remember(editingEntry, dbState.subjects) {
        mutableStateOf(
            editingEntry?.subjectId ?: dbState.subjects.firstOrNull()?.id.orEmpty()
        )
    }
    val selectedSubject: Subject? = dbState.subjects.firstOrNull { it.id == selectedSubjectId }

    var isLab by remember(editingEntry, selectedSubject) {
        mutableStateOf(
            editingEntry?.isLab ?: (selectedSubject?.isEvenCode == true)
        )
    }

    var selectedTeacher by remember(editingEntry) {
        mutableStateOf(editingEntry?.teacher.orEmpty())
    }

    var selectedDay by remember(editingEntry, prefillDay) {
        mutableIntStateOf(editingEntry?.dayOfWeek ?: (prefillDay ?: 0).coerceIn(0, 4))
    }

    val dynamicGapParity = remember(editingEntry, initialParity) {
        when {
            editingEntry != null && editingEntry.repeatPattern != RepeatPattern.EVERY_WEEK ->
                editingEntry.repeatPattern
            initialParity != RepeatPattern.EVERY_WEEK -> initialParity
            else -> RepeatPattern.ODD_WEEKS
        }
    }

    var repeatPattern by remember(editingEntry, initialParity) {
        mutableIntStateOf(
            editingEntry?.repeatPattern ?: if (isLab && initialParity != RepeatPattern.EVERY_WEEK) {
                initialParity
            } else {
                RepeatPattern.EVERY_WEEK
            }
        )
    }

    var selectedSlot by remember(editingEntry, prefillSlot) {
        mutableStateOf(editingEntry?.startSlot ?: prefillSlot)
    }

    var activeSheet by remember { mutableStateOf<AddSheetType?>(null) }

    // Keep subject/type/teacher/slot invariants strictly conflict-free
    LaunchedEffect(selectedSubjectId) {
        val subj = dbState.subjects.firstOrNull { it.id == selectedSubjectId }
        if (subj != null) {
            if (subj.isEvenCode) {
                isLab = true
            }
            if (selectedTeacher.isBlank() || (subj.teachers.isNotEmpty() && selectedTeacher !in subj.teachers)) {
                selectedTeacher = subj.teachers.firstOrNull().orEmpty()
            }
        }
    }

    LaunchedEffect(isLab, selectedDay, repeatPattern, dbState.entries) {
        if (!isLab) {
            repeatPattern = RepeatPattern.EVERY_WEEK
        }
        val span = if (isLab) 3 else 1
        val current = selectedSlot
        val validBlock = !isLab || current in ScheduleSlots.labBlockStartSlots
        val occupied = current == null || !validBlock || RoutineScheduleEngine.isSlotOccupied(
            entries = dbState.entries,
            dayOfWeek = selectedDay,
            startSlot = current,
            span = span,
            repeatPattern = repeatPattern,
            excludeEntryId = editEntryId
        )
        if (occupied) {
            selectedSlot = RoutineScheduleEngine.findFirstAvailableSlot(
                entries = dbState.entries,
                dayOfWeek = selectedDay,
                isLab = isLab,
                repeatPattern = repeatPattern,
                excludeEntryId = editEntryId
            )
        }
    }

    val canSave = selectedSubject != null && selectedSlot != null

    Box(modifier = Modifier.fillMaxSize().background(RoutineColors.Black)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = if (editingEntry != null) "Edit Class" else "Add Class",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        val slot = selectedSlot ?: return@Button
                        val subj = selectedSubject ?: return@Button
                        val saved = onSave(
                            editEntryId,
                            subj.id,
                            isLab,
                            selectedTeacher,
                            selectedDay,
                            slot,
                            repeatPattern
                        )
                        if (saved) onBack()
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoutineColors.PrimaryPill,
                        contentColor = RoutineColors.PrimaryPillText,
                        disabledContainerColor = RoutineColors.SurfaceElevated,
                        disabledContentColor = RoutineColors.TextMuted
                    )
                ) {
                    Text(
                        text = "Save",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Subject, Type, Teacher
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = RoutineColors.SurfaceCard,
                    border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Subject Row
                        val grade = selectedSubject?.let { ColorPalette.byId(it.colorId) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeSheet = AddSheetType.SUBJECT }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FormRowLeading(
                                icon = Icons.Rounded.Book,
                                iconBg = Color(0xFF1A2438),
                                iconTint = RoutineColors.PrimaryPill,
                                label = "Subject"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (selectedSubject != null && grade != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isLab) grade.labStart else grade.accent)
                                    )
                                    Text(
                                        text = selectedSubject.displayCodeFor(isLab),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "Select Subject",
                                        fontSize = 15.sp,
                                        color = RoutineColors.TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = RoutineColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = RoutineColors.BorderSubtle)

                        // Type Row (Class | Lab)
                        val lockToLab = selectedSubject?.isEvenCode == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FormRowLeading(
                                icon = Icons.Rounded.LocalOffer,
                                iconBg = Color(0xFF331419),
                                iconTint = Color(0xFFEF4444),
                                label = "Type"
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0A0C10))
                                    .border(1.dp, RoutineColors.BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(3.dp)
                            ) {
                                val classEnabled = !lockToLab
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (!isLab) Color(0xFF323744) else Color.Transparent)
                                        .alpha(if (classEnabled) 1f else 0.3f)
                                        .clickable(enabled = classEnabled) { isLab = false }
                                        .padding(horizontal = 16.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = "Class",
                                        fontSize = 13.sp,
                                        fontWeight = if (!isLab) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isLab) Color.White else RoutineColors.TextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (isLab) Color(0xFF323744) else Color.Transparent)
                                        .clickable {
                                            isLab = true
                                            if (initialParity != RepeatPattern.EVERY_WEEK && editingEntry == null) {
                                                repeatPattern = initialParity
                                            }
                                        }
                                        .padding(horizontal = 18.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = "Lab",
                                        fontSize = 13.sp,
                                        fontWeight = if (isLab) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isLab) Color.White else RoutineColors.TextSecondary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = RoutineColors.BorderSubtle)

                        // Teacher Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeSheet = AddSheetType.TEACHER }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FormRowLeading(
                                icon = Icons.Rounded.Groups,
                                iconBg = Color(0xFF281538),
                                iconTint = Color(0xFFA855F7),
                                label = "Teacher"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = selectedTeacher.ifBlank { "Select Teacher" },
                                    fontSize = 15.sp,
                                    color = if (selectedTeacher.isBlank()) RoutineColors.TextMuted else Color.White,
                                    fontWeight = if (selectedTeacher.isBlank()) FontWeight.Normal else FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = RoutineColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Card 2: Day, Time, Repeat
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = RoutineColors.SurfaceCard,
                    border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Day Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeSheet = AddSheetType.DAY }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FormRowLeading(
                                icon = Icons.Rounded.CalendarToday,
                                iconBg = Color(0xFF33210F),
                                iconTint = RoutineColors.AmberWarning,
                                label = "Day"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0A0C10),
                                    border = BorderStroke(1.dp, RoutineColors.BorderSubtle)
                                ) {
                                    Text(
                                        text = RoutineDate.FULL_DAY_NAMES[selectedDay],
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = RoutineColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = RoutineColors.BorderSubtle)

                        // Time Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeSheet = AddSheetType.TIME }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FormRowLeading(
                                icon = Icons.Rounded.Schedule,
                                iconBg = Color(0xFF0F2D1D),
                                iconTint = RoutineColors.EmeraldSuccess,
                                label = "Time"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val slotVal = selectedSlot
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0A0C10),
                                    border = BorderStroke(1.dp, RoutineColors.BorderSubtle)
                                ) {
                                    Text(
                                        text = if (slotVal != null) {
                                            ScheduleSlots.compactRangeLabel(slotVal, if (isLab) 3 else 1)
                                        } else {
                                            "Day Full"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (slotVal != null) Color.White else RoutineColors.DangerRed,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = RoutineColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = RoutineColors.BorderSubtle)

                        // Repeat Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isLab) { activeSheet = AddSheetType.REPEAT }
                                .alpha(if (isLab) 1f else 0.7f)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FormRowLeading(
                                icon = Icons.Rounded.Repeat,
                                iconBg = Color(0xFF1D222D),
                                iconTint = Color.White,
                                label = "Repeat"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = repeatLabel(repeatPattern),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isLab && repeatPattern != RepeatPattern.EVERY_WEEK) {
                                        RoutineColors.CyanAccent
                                    } else {
                                        Color.White
                                    }
                                )
                                if (isLab) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = RoutineColors.TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedSubject == null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = RoutineColors.SurfaceCard,
                        border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onManageSubjects() }
                    ) {
                        Text(
                            text = "No subjects found. Tap here to add a subject first.",
                            color = RoutineColors.PrimaryPill,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Picker Sheets
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        if (activeSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = null },
                sheetState = sheetState,
                containerColor = RoutineColors.SurfaceDark,
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.45f)
            ) {
                when (activeSheet) {
                    AddSheetType.SUBJECT -> {
                        PickerSheetContainer(title = "Select Subject") {
                            dbState.subjects.forEach { subj ->
                                val g = ColorPalette.byId(subj.colorId)
                                val selected = subj.id == selectedSubjectId
                                PickerOptionRow(
                                    title = subj.code,
                                    subtitle = subj.teachers.joinToString(", ").ifBlank { null },
                                    leadingDot = g.accent,
                                    selected = selected,
                                    enabled = true,
                                    onClick = {
                                        selectedSubjectId = subj.id
                                        activeSheet = null
                                    }
                                )
                            }
                            if (dbState.subjects.isEmpty()) {
                                PickerOptionRow(
                                    title = "Add Subject",
                                    subtitle = "Go to Manage Subjects",
                                    leadingDot = RoutineColors.PrimaryPill,
                                    selected = false,
                                    enabled = true,
                                    onClick = {
                                        activeSheet = null
                                        onManageSubjects()
                                    }
                                )
                            }
                        }
                    }

                    AddSheetType.TEACHER -> {
                        val teachers = selectedSubject?.teachers.orEmpty()
                        PickerSheetContainer(title = "Select Teacher") {
                            PickerOptionRow(
                                title = "None",
                                subtitle = "No instructor shown",
                                leadingDot = null,
                                selected = selectedTeacher.isBlank(),
                                enabled = true,
                                onClick = {
                                    selectedTeacher = ""
                                    activeSheet = null
                                }
                            )
                            teachers.forEach { t ->
                                PickerOptionRow(
                                    title = t,
                                    subtitle = null,
                                    leadingDot = null,
                                    selected = selectedTeacher == t,
                                    enabled = true,
                                    onClick = {
                                        selectedTeacher = t
                                        activeSheet = null
                                    }
                                )
                            }
                        }
                    }

                    AddSheetType.DAY -> {
                        PickerSheetContainer(title = "Select Day") {
                            ScheduleSlots.workingDaysRange.forEach { dayIdx ->
                                PickerOptionRow(
                                    title = RoutineDate.FULL_DAY_NAMES[dayIdx],
                                    subtitle = null,
                                    leadingDot = null,
                                    selected = selectedDay == dayIdx,
                                    enabled = true,
                                    onClick = {
                                        selectedDay = dayIdx
                                        activeSheet = null
                                    }
                                )
                            }
                        }
                    }

                    AddSheetType.TIME -> {
                        PickerSheetContainer(
                            title = if (isLab) "Select Lab Block (2h 30m)" else "Select Time Slot (50m)"
                        ) {
                            val span = if (isLab) 3 else 1
                            val slotIndices = if (isLab) {
                                ScheduleSlots.labBlockStartSlots
                            } else {
                                ScheduleSlots.slots.indices.toList()
                            }
                            slotIndices.forEach { idx ->
                                val occupier = findOccupyingEntry(
                                    entries = dbState.entries,
                                    subjects = dbState.subjects,
                                    dayOfWeek = selectedDay,
                                    slot = idx,
                                    span = span,
                                    repeatPattern = repeatPattern,
                                    excludeEntryId = editEntryId
                                )
                                val enabled = occupier == null
                                PickerOptionRow(
                                    title = ScheduleSlots.timeRangeLabel(idx, span),
                                    subtitle = if (occupier != null) "Occupied by $occupier" else "Available",
                                    leadingDot = if (enabled) RoutineColors.EmeraldSuccess else RoutineColors.TextMuted,
                                    selected = selectedSlot == idx,
                                    enabled = enabled,
                                    onClick = {
                                        selectedSlot = idx
                                        activeSheet = null
                                    }
                                )
                            }
                        }
                    }

                    AddSheetType.REPEAT -> {
                        PickerSheetContainer(title = "Lab Repeat Frequency") {
                            val currentSlot = selectedSlot ?: 0
                            val options = listOf(
                                Triple(RepeatPattern.EVERY_WEEK, "Every week", "Repeats every week"),
                                Triple(dynamicGapParity, "Every 2 weeks (1-week gap)", "Repeats every 2 weeks from this week")
                            )
                            options.forEach { (pattern, label, desc) ->
                                val occupier = findOccupyingEntry(
                                    entries = dbState.entries,
                                    subjects = dbState.subjects,
                                    dayOfWeek = selectedDay,
                                    slot = currentSlot,
                                    span = 3,
                                    repeatPattern = pattern,
                                    excludeEntryId = editEntryId
                                )
                                // Allow picking a pattern even if current slot is occupied if another block is free
                                val anyFree = RoutineScheduleEngine.findFirstAvailableSlot(
                                    entries = dbState.entries,
                                    dayOfWeek = selectedDay,
                                    isLab = true,
                                    repeatPattern = pattern,
                                    excludeEntryId = editEntryId
                                ) != null
                                PickerOptionRow(
                                    title = label,
                                    subtitle = if (occupier != null && !anyFree) "All blocks occupied" else if (occupier != null) "Will auto-select next free block" else desc,
                                    leadingDot = if (anyFree) RoutineColors.CyanAccent else RoutineColors.TextMuted,
                                    selected = repeatPattern == pattern,
                                    enabled = anyFree,
                                    onClick = {
                                        repeatPattern = pattern
                                        activeSheet = null
                                    }
                                )
                            }
                        }
                    }

                    null -> Unit
                }
            }
        }
    }
}

private fun repeatLabel(pattern: Int): String = when (pattern) {
    RepeatPattern.EVERY_WEEK -> "Every week"
    else -> "1-week gap"
}

private fun findOccupyingEntry(
    entries: List<RoutineEntry>,
    subjects: List<Subject>,
    dayOfWeek: Int,
    slot: Int,
    span: Int,
    repeatPattern: Int,
    excludeEntryId: String?
): String? {
    val hit = entries.firstOrNull { existing ->
        existing.id != excludeEntryId &&
            existing.dayOfWeek == dayOfWeek &&
            existing.overlapsSlotRange(slot, span) &&
            RepeatPattern.conflicts(existing.repeatPattern, repeatPattern)
    } ?: return null
    val subj = subjects.firstOrNull { it.id == hit.subjectId }
    return subj?.displayCodeFor(hit.isLab) ?: "Another class"
}

@Composable
private fun FormRowLeading(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun PickerSheetContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun PickerOptionRow(
    title: String,
    subtitle: String?,
    leadingDot: Color?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) RoutineColors.SurfaceElevated else RoutineColors.SurfaceCard,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) RoutineColors.PrimaryPill.copy(alpha = 0.65f) else RoutineColors.BorderSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.35f)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (leadingDot != null) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(leadingDot)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = RoutineColors.TextSecondary
                        )
                    }
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = RoutineColors.PrimaryPill,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
