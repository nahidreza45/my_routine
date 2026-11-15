package com.nahidreza.routine

import com.nahidreza.routine.data.RoutineRepository
import com.nahidreza.routine.data.RoutineScheduleEngine
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.AppSettings
import com.nahidreza.routine.model.CancelledInstance
import com.nahidreza.routine.model.CtCourseRecord
import com.nahidreza.routine.model.RepeatPattern
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.RoutineEntry
import com.nahidreza.routine.model.SharedScheduleBackup
import com.nahidreza.routine.model.Subject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineEngineTest {

    @Test
    fun `odd subject code increments by 1 for lab and even code stays unchanged`() {
        val theory = Subject("1", "ECE 3117", "red", listOf("HBK"))
        assertFalse(theory.isEvenCode)
        assertEquals("ECE 3117", theory.displayCodeFor(isLab = false))
        assertEquals("ECE 3118", theory.displayCodeFor(isLab = true))

        val standaloneLab = Subject("2", "CE 3100", "teal", listOf("ST"))
        assertTrue(standaloneLab.isEvenCode)
        assertEquals("CE 3100", standaloneLab.displayCodeFor(isLab = true))
    }

    @Test
    fun `two alternating labs can occupy same Sunday morning block on opposite weeks`() {
        val labOdd = RoutineEntry(
            id = "l1",
            subjectId = "sub_3117",
            isLab = true,
            teacher = "HBK",
            dayOfWeek = 1,
            startSlot = 0,
            span = 3,
            repeatPattern = RepeatPattern.ODD_WEEKS
        )
        val entries = listOf(labOdd)

        // Odd week slot is occupied; Even week slot is FREE
        assertTrue(RoutineScheduleEngine.isSlotOccupied(entries, dayOfWeek = 1, startSlot = 0, span = 3, repeatPattern = RepeatPattern.ODD_WEEKS))
        assertTrue(RoutineScheduleEngine.isSlotOccupied(entries, dayOfWeek = 1, startSlot = 0, span = 3, repeatPattern = RepeatPattern.EVERY_WEEK))
        assertFalse(RoutineScheduleEngine.isSlotOccupied(entries, dayOfWeek = 1, startSlot = 0, span = 3, repeatPattern = RepeatPattern.EVEN_WEEKS))

        // Shifting labOdd is allowed while Even week is free
        assertTrue(RoutineScheduleEngine.canShiftLabNextWeek(entries, labOdd))

        // Adding labEven in same slot blocks Shift Next Week for both
        val labEven = labOdd.copy(id = "l2", subjectId = "sub_3111", repeatPattern = RepeatPattern.EVEN_WEEKS)
        assertFalse(RoutineScheduleEngine.canShiftLabNextWeek(listOf(labOdd, labEven), labOdd))
    }

    @Test
    fun `ct marks takes best 3 divided by 3 and applies ceiling`() {
        val partial = CtCourseRecord("sub_1", listOf(19, 17, null, null))
        assertEquals(12, partial.bestThreeAverageCeil()) // (19 + 17 + 0) / 3 = 12

        val fourCts = CtCourseRecord("sub_2", listOf(18, 16, 19, 14))
        assertEquals(18, fourCts.bestThreeAverageCeil()) // (19 + 18 + 16) / 3 = 53 / 3 = 17.66 -> ceil = 18

        val legacyFifthIgnored = CtCourseRecord("sub_3", listOf(10, 18, 17, 15, 20), showFifth = true)
        assertEquals(17, legacyFifthIgnored.bestThreeAverageCeil()) // best 3 of first 4: (18 + 17 + 15) / 3 = 16.66 -> ceil = 17
    }

    @Test
    fun `university week starts on Saturday and snaps Thu Fri to Saturday`() {
        val sat = RoutineDate(2026, 11, 14) // Saturday Nov 14, 2026
        assertEquals(0, sat.satBasedDayOfWeek)
        assertEquals(1, sat.academicWeekNumber(sat, 0))

        val sunNextWeek = sat.plusDays(8)
        assertEquals(2, sunNextWeek.academicWeekNumber(sat, 0))

        val thu = sat.plusDays(5)
        assertFalse(thu.isWorkingDay)
        assertEquals(sat.plusDays(7), RoutineDate.nearestActiveOrNextWorkingDate(thu))
    }

    @Test
    fun `merging shared schedule preserves personal CT marks and app preferences`() {
        val personalCt = listOf(CtCourseRecord("sub_1", listOf(19, 18, 17, null)))
        val existingState = AppDatabaseState(
            subjects = listOf(Subject("sub_1", "ECE 3117", "red", listOf("HBK"))),
            entries = listOf(RoutineEntry("e1", "sub_1", false, "HBK", 1, 0, 1)),
            cancellations = listOf(
                CancelledInstance("e1", "2026-11-15"),
                CancelledInstance("removed_entry", "2026-11-16")
            ),
            ctRecords = personalCt,
            settings = AppSettings(
                semesterStartDateIso = "2026-11-07",
                manualWeekOffset = 0,
                viewMode = "WEEK",
                notificationsEnabled = false,
                notificationLeadMinutes = 15
            )
        )
        val shared = SharedScheduleBackup(
            subjects = listOf(Subject("sub_1", "ECE 3117", "red", listOf("MNT"))),
            entries = listOf(RoutineEntry("e1", "sub_1", false, "MNT", 1, 0, 1)),
            semesterStartDateIso = "2026-11-14",
            manualWeekOffset = 1
        )

        val merged = RoutineRepository.mergeSharedSchedule(existingState, shared)
        assertEquals(personalCt, merged.ctRecords)
        assertEquals("WEEK", merged.settings.viewMode)
        assertFalse(merged.settings.notificationsEnabled)
        assertEquals(15, merged.settings.notificationLeadMinutes)
        assertEquals("2026-11-14", merged.settings.semesterStartDateIso)
        assertEquals(1, merged.settings.manualWeekOffset)
        assertEquals(listOf(CancelledInstance("e1", "2026-11-15")), merged.cancellations)
    }
}
