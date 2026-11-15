package com.nahidreza.routine.model

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Lightweight, immutable calendar date backed by Gregorian Calendar math (API 24+ safe).
 * Working day indices use Saturday = 0 .. Wednesday = 4 (Thursday = 5, Friday = 6).
 */
data class RoutineDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) : Comparable<RoutineDate> {

    override fun compareTo(other: RoutineDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    /** Returns 0 = SAT, 1 = SUN, 2 = MON, 3 = TUE, 4 = WED, 5 = THU, 6 = FRI. */
    val satBasedDayOfWeek: Int
        get() {
            val cal = utcNoonCalendar()
            return cal.get(Calendar.DAY_OF_WEEK) % 7
        }

    val isWorkingDay: Boolean
        get() = satBasedDayOfWeek in 0..4

    val shortDayName: String
        get() = SHORT_DAY_NAMES[satBasedDayOfWeek]

    val fullDayName: String
        get() = FULL_DAY_NAMES[satBasedDayOfWeek]

    val monthName: String
        get() = MONTH_NAMES.getOrElse(month - 1) { "" }

    val shortMonthName: String
        get() = SHORT_MONTH_NAMES.getOrElse(month - 1) { "" }

    fun toIso(): String =
        String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    fun toShortDisplay(): String =
        "$shortMonthName $day, $year"

    fun plusDays(days: Int): RoutineDate {
        val cal = utcNoonCalendar()
        cal.add(Calendar.DAY_OF_MONTH, days)
        return RoutineDate(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun startOfUniversityWeek(): RoutineDate = plusDays(-satBasedDayOfWeek)

    fun daysUntil(other: RoutineDate): Long {
        val msDiff = other.utcNoonCalendar().timeInMillis - this.utcNoonCalendar().timeInMillis
        return Math.round(msDiff / 86_400_000.0)
    }

    fun academicWeekNumber(semesterStart: RoutineDate, manualOffset: Int): Int {
        val semSat = semesterStart.startOfUniversityWeek()
        val thisSat = this.startOfUniversityWeek()
        val weekDiff = Math.floorDiv(semSat.daysUntil(thisSat), 7L).toInt()
        return weekDiff + 1 + manualOffset
    }

    fun toUtcMidnightMillis(): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun toEpochMillisAt(hour24: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun utcNoonCalendar(): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    companion object {
        val SHORT_DAY_NAMES = listOf("SAT", "SUN", "MON", "TUE", "WED", "THU", "FRI")
        val FULL_DAY_NAMES = listOf(
            "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
        )
        private val MONTH_NAMES = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        private val SHORT_MONTH_NAMES = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        fun today(): RoutineDate {
            val cal = Calendar.getInstance()
            return RoutineDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromEpochMillis(millis: Long): RoutineDate {
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            return RoutineDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromUtcMillis(millis: Long): RoutineDate {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
            return RoutineDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromIso(iso: String): RoutineDate? {
            val parts = iso.trim().split("-")
            if (parts.size != 3) return null
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            if (m !in 1..12 || d !in 1..31) return null
            return RoutineDate(y, m, d)
        }

        /** Snaps Thursday/Friday to the next upcoming Saturday so Day mode lands on an active working day. */
        fun nearestActiveOrNextWorkingDate(date: RoutineDate = today()): RoutineDate {
            return when (date.satBasedDayOfWeek) {
                in 0..4 -> date
                5 -> date.plusDays(2) // Thursday -> Saturday
                else -> date.plusDays(1) // Friday -> Saturday
            }
        }
    }
}
