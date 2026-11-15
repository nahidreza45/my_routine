package com.nahidreza.routine.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlin.math.ceil

@Immutable
data class SubjectColorGrade(
    val id: String,
    val name: String,
    val theoryStartArgb: Long,
    val theoryEndArgb: Long,
    val labStartArgb: Long,
    val labEndArgb: Long,
    val accentArgb: Long
) {
    val theoryStart: Color get() = Color(theoryStartArgb)
    val theoryEnd: Color get() = Color(theoryEndArgb)
    val labStart: Color get() = Color(labStartArgb)
    val labEnd: Color get() = Color(labEndArgb)
    val accent: Color get() = Color(accentArgb)

    fun gradientBrush(isLabTone: Boolean): Brush = ColorPalette.brushFor(id, isLabTone)
}

object ColorPalette {
    val grades: List<SubjectColorGrade> = listOf(
        SubjectColorGrade("blue", "Ocean Blue", 0xFF1E7AE0, 0xFF0A397F, 0xFF0E4596, 0xFF04193B, 0xFF3B9BFF),
        SubjectColorGrade("purple", "Royal Purple", 0xFF932CB8, 0xFF431180, 0xFF6A1B9A, 0xFF23063D, 0xFFBD66E8),
        SubjectColorGrade("green", "Emerald Green", 0xFF2B8A3E, 0xFF134E20, 0xFF185E29, 0xFF072910, 0xFF40C057),
        SubjectColorGrade("red", "Crimson Red", 0xFFD93838, 0xFF7D0A0A, 0xFF961717, 0xFF3D0505, 0xFFFF5A5A),
        SubjectColorGrade("orange", "Burnt Orange", 0xFFEB571C, 0xFF9E2B07, 0xFFB83309, 0xFF4A1202, 0xFFFF7A3D),
        SubjectColorGrade("teal", "Deep Teal", 0xFF00897B, 0xFF004339, 0xFF00594E, 0xFF00211C, 0xFF20C997),
        SubjectColorGrade("rose", "Rose Magenta", 0xFFD01D65, 0xFF750B3E, 0xFF8A0E49, 0xFF38041E, 0xFFF04E8E),
        SubjectColorGrade("indigo", "Indigo Royal", 0xFF3B4CCA, 0xFF161F73, 0xFF1F2887, 0xFF080D36, 0xFF5C6EFF),
        SubjectColorGrade("cyan", "Cobalt Cyan", 0xFF0091A1, 0xFF004954, 0xFF005B69, 0xFF00242B, 0xFF22D3EE),
        SubjectColorGrade("amber", "Amber Bronze", 0xFFD97D06, 0xFF804000, 0xFF964A00, 0xFF401E00, 0xFFF59E0B),
        SubjectColorGrade("plum", "Plum Berry", 0xFFB81860, 0xFF5E002B, 0xFF750032, 0xFF330013, 0xFFE8438B),
        SubjectColorGrade("pine", "Forest Pine", 0xFF1F7A5C, 0xFF0A3326, 0xFF104A37, 0xFF051C14, 0xFF34B388),
        SubjectColorGrade("violet", "Electric Violet", 0xFF6B3FD1, 0xFF2A1480, 0xFF43219C, 0xFF150747, 0xFF9775FA),
        SubjectColorGrade("wine", "Ruby Wine", 0xFFC22121, 0xFF5C0707, 0xFF7A0E0E, 0xFF330404, 0xFFEB5757),
        SubjectColorGrade("navy", "Steel Navy", 0xFF1B6EC8, 0xFF082E63, 0xFF0D3E7D, 0xFF041633, 0xFF4DABF7),
        SubjectColorGrade("slate", "Graphite Slate", 0xFF4B606B, 0xFF212B30, 0xFF2A363D, 0xFF0F1417, 0xFF78909C)
    )

    private val gradeById: Map<String, SubjectColorGrade> = grades.associateBy { it.id }

    private val theoryBrushById: Map<String, Brush> = grades.associate { g ->
        g.id to Brush.linearGradient(
            colors = listOf(g.theoryStart, g.theoryEnd),
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    private val labBrushById: Map<String, Brush> = grades.associate { g ->
        g.id to Brush.linearGradient(
            colors = listOf(g.labStart, g.labEnd),
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    fun byId(id: String): SubjectColorGrade = gradeById[id] ?: grades.first()

    fun brushFor(id: String, isLabTone: Boolean): Brush {
        return if (isLabTone) {
            labBrushById[id] ?: labBrushById.getValue(grades.first().id)
        } else {
            theoryBrushById[id] ?: theoryBrushById.getValue(grades.first().id)
        }
    }

    fun firstUnusedColorId(existingSubjects: List<Subject>): String {
        val used = existingSubjects.map { it.colorId }.toSet()
        return grades.firstOrNull { it.id !in used }?.id ?: grades[existingSubjects.size % grades.size].id
    }
}

@Immutable
data class TimeSlotSpec(
    val index: Int,
    val startHour24: Int,
    val startMinute: Int,
    val endHour24: Int,
    val endMinute: Int,
    val startLabel: String,
    val endLabel: String,
    val axisLabel: String
) {
    val startMinutesOfDay: Int get() = startHour24 * 60 + startMinute
    val endMinutesOfDay: Int get() = endHour24 * 60 + endMinute
}

object ScheduleSlots {
    val workingDaysRange = 0..4

    val slots: List<TimeSlotSpec> = listOf(
        TimeSlotSpec(0, 8, 0, 8, 50, "8:00 AM", "8:50 AM", "8 am"),
        TimeSlotSpec(1, 8, 50, 9, 40, "8:50 AM", "9:40 AM", "8:50"),
        TimeSlotSpec(2, 9, 40, 10, 30, "9:40 AM", "10:30 AM", "9:40"),
        TimeSlotSpec(3, 10, 50, 11, 40, "10:50 AM", "11:40 AM", "10:50"),
        TimeSlotSpec(4, 11, 40, 12, 30, "11:40 AM", "12:30 PM", "11:40"),
        TimeSlotSpec(5, 12, 30, 13, 20, "12:30 PM", "1:20 PM", "12:30"),
        TimeSlotSpec(6, 14, 30, 15, 20, "2:30 PM", "3:20 PM", "2:30"),
        TimeSlotSpec(7, 15, 20, 16, 10, "3:20 PM", "4:10 PM", "3:20"),
        TimeSlotSpec(8, 16, 10, 17, 0, "4:10 PM", "5:00 PM", "4:10")
    )

    val labBlockStartSlots: List<Int> = listOf(0, 3, 6)

    fun timeRangeLabel(startSlot: Int, span: Int): String {
        val first = slots.getOrElse(startSlot) { slots.first() }
        val last = slots.getOrElse((startSlot + span - 1).coerceIn(0, slots.lastIndex)) { first }
        return "${first.startLabel} \u2014 ${last.endLabel}"
    }

    fun compactRangeLabel(startSlot: Int, span: Int): String {
        val first = slots.getOrElse(startSlot) { slots.first() }
        val last = slots.getOrElse((startSlot + span - 1).coerceIn(0, slots.lastIndex)) { first }
        return "${first.startLabel} \u2192 ${last.endLabel}"
    }
}

object RepeatPattern {
    const val EVERY_WEEK = 0
    const val ODD_WEEKS = 1
    const val EVEN_WEEKS = 2

    fun parityForAcademicWeek(academicWeek: Int): Int {
        return if (Math.floorMod(academicWeek - 1, 2) == 0) ODD_WEEKS else EVEN_WEEKS
    }

    fun conflicts(p1: Int, p2: Int): Boolean {
        if (p1 == EVERY_WEEK || p2 == EVERY_WEEK) return true
        return p1 == p2
    }
}

@Serializable
@Immutable
data class Subject(
    val id: String,
    val code: String,
    val colorId: String,
    val teachers: List<String> = emptyList()
) {
    /** True when the trailing course number is even (e.g., CE 3100, ECE 3100). */
    val isEvenCode: Boolean
        get() {
            val num = TRAILING_NUMBER_REGEX.find(code.trim())?.groupValues?.getOrNull(1)?.toIntOrNull()
            return num != null && num % 2 == 0
        }

    /** If a theory course (odd code) is scheduled as a Lab, its lab code increments by +1 (e.g., 3117 -> 3118). */
    fun displayCodeFor(isLab: Boolean): String {
        if (!isLab || isEvenCode) return code
        val match = TRAILING_NUMBER_REGEX.find(code.trim()) ?: return code
        val digits = match.groupValues[1]
        val nextNum = digits.toIntOrNull()?.plus(1) ?: return code
        val padded = nextNum.toString().padStart(digits.length, '0')
        return code.trim().replaceRange(match.range, padded)
    }

    companion object {
        private val TRAILING_NUMBER_REGEX = Regex("(\\d+)\\s*$")
    }
}

@Serializable
@Immutable
data class RoutineEntry(
    val id: String,
    val subjectId: String,
    val isLab: Boolean,
    val teacher: String,
    val dayOfWeek: Int, // 0 = SAT .. 4 = WED
    val startSlot: Int, // 0..8 (for labs: 0, 3, or 6)
    val span: Int,      // 1 for Theory, 3 for Lab
    val repeatPattern: Int = RepeatPattern.EVERY_WEEK,
    val parityShiftHistory: Map<Int, Int> = emptyMap()
) {
    fun parityForAcademicWeek(academicWeek: Int): Int {
        return parityShiftHistory[academicWeek] ?: repeatPattern
    }

    fun isActiveInAcademicWeek(academicWeek: Int): Boolean {
        val effectiveParity = parityForAcademicWeek(academicWeek)
        if (effectiveParity == RepeatPattern.EVERY_WEEK) return true
        return effectiveParity == RepeatPattern.parityForAcademicWeek(academicWeek)
    }

    fun overlapsSlotRange(otherStart: Int, otherSpan: Int): Boolean {
        val thisEnd = startSlot + span
        val otherEnd = otherStart + otherSpan
        return startSlot < otherEnd && otherStart < thisEnd
    }
}

@Serializable
@Immutable
data class CancelledInstance(
    val entryId: String,
    val dateIso: String
)

@Serializable
@Immutable
data class HolidayRange(
    val id: String,
    val name: String,
    val startDateIso: String,
    val endDateIso: String
) {
    fun contains(date: RoutineDate): Boolean {
        val s = RoutineDate.fromIso(startDateIso) ?: return false
        val e = RoutineDate.fromIso(endDateIso) ?: return false
        return date >= s && date <= e
    }
}

@Serializable
@Immutable
data class CtCourseRecord(
    val subjectId: String,
    val marks: List<Int?> = listOf(null, null, null, null),
    val showFifth: Boolean = false
) {
    fun activeMarks(): List<Int?> {
        return (0 until 4).map { idx -> marks.getOrNull(idx) }
    }

    fun bestThreeIndices(): Set<Int> {
        val scored = activeMarks().mapIndexedNotNull { index, score ->
            score?.let { index to it }
        }
        return scored
            .sortedWith(compareByDescending<Pair<Int, Int>> { it.second }.thenBy { it.first })
            .take(3)
            .map { it.first }
            .toSet()
    }

    fun bestThreeAverageCeil(): Int {
        val top3Sum = activeMarks()
            .map { (it ?: 0).coerceIn(0, 20) }
            .sortedDescending()
            .take(3)
            .sum()
        return ceil(top3Sum / 3.0).toInt()
    }
}

@Serializable
@Immutable
data class AppSettings(
    val semesterStartDateIso: String = RoutineDate.today().startOfUniversityWeek().toIso(),
    val manualWeekOffset: Int = 0,
    val viewMode: String = "DAY", // "DAY" or "WEEK"
    val notificationsEnabled: Boolean = true,
    val notificationLeadMinutes: Int = 10
)

@Serializable
@Immutable
data class AppDatabaseState(
    val subjects: List<Subject> = emptyList(),
    val entries: List<RoutineEntry> = emptyList(),
    val cancellations: List<CancelledInstance> = emptyList(),
    val holidays: List<HolidayRange> = emptyList(),
    val ctRecords: List<CtCourseRecord> = emptyList(),
    val settings: AppSettings = AppSettings()
)
