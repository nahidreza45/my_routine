package com.nahidreza.routine.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nahidreza.routine.data.RoutineScheduleEngine
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.ColorPalette
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.ScheduleSlots

object AlarmScheduler {
    private const val PREFS_NAME = "routine_alarm_registry"
    private const val KEY_IDS = "scheduled_ids"

    fun scheduleUpcomingAlarms(context: Context, state: AppDatabaseState) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Cancel previously scheduled alarms to guarantee zero stale notifications
        val oldIds = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty()
        for (rawId in oldIds) {
            val reqCode = rawId.toIntOrNull() ?: continue
            val cancelIntent = Intent(context, ClassAlarmReceiver::class.java).apply {
                action = ClassAlarmReceiver.ACTION_FIRE_CLASS_ALERT
            }
            val pi = PendingIntent.getBroadcast(
                context,
                reqCode,
                cancelIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) {
                alarmManager.cancel(pi)
                pi.cancel()
            }
        }

        if (!state.settings.notificationsEnabled) {
            prefs.edit().putStringSet(KEY_IDS, emptySet()).apply()
            return
        }

        val nowMillis = System.currentTimeMillis()
        val today = RoutineDate.today()
        val leadMinutes = state.settings.notificationLeadMinutes.coerceIn(1, 60)
        val leadMillis = leadMinutes * 60_000L
        val newIds = HashSet<String>()

        // Keep a compact 7-day rolling window (refreshed whenever alerts fire, on app open, or on boot/update)
        for (dayOffset in 0..6) {
            val date = today.plusDays(dayOffset)
            if (!date.isWorkingDay) continue
            if (RoutineScheduleEngine.isHoliday(date, state.holidays) != null) continue

            val resolved = RoutineScheduleEngine.entriesForDate(date, state)
                .filter { !it.isCancelled }

            for (item in resolved) {
                val startSpec = ScheduleSlots.slots.getOrNull(item.entry.startSlot) ?: continue
                val classStartMillis = date.toEpochMillisAt(startSpec.startHour24, startSpec.startMinute)
                val triggerAt = classStartMillis - leadMillis
                if (triggerAt <= nowMillis) continue

                val dateIso = date.toIso()
                val reqCode = ("${item.entry.id}_$dateIso").hashCode() and 0x7FFFFFFF
                val colorGrade = ColorPalette.byId(item.subject.colorId)
                val timeRange = ScheduleSlots.compactRangeLabel(item.entry.startSlot, item.entry.span)

                val intent = Intent(context, ClassAlarmReceiver::class.java).apply {
                    action = ClassAlarmReceiver.ACTION_FIRE_CLASS_ALERT
                    putExtra(ClassAlarmReceiver.EXTRA_NOTIF_ID, reqCode)
                    putExtra(ClassAlarmReceiver.EXTRA_ENTRY_ID, item.entry.id)
                    putExtra(ClassAlarmReceiver.EXTRA_DATE_ISO, dateIso)
                    putExtra(ClassAlarmReceiver.EXTRA_DISPLAY_CODE, item.displayCode)
                    putExtra(ClassAlarmReceiver.EXTRA_IS_LAB, item.entry.isLab)
                    putExtra(ClassAlarmReceiver.EXTRA_TEACHER, item.entry.teacher)
                    putExtra(ClassAlarmReceiver.EXTRA_TIME_RANGE, timeRange)
                    putExtra(ClassAlarmReceiver.EXTRA_LEAD_MINUTES, leadMinutes)
                    putExtra(ClassAlarmReceiver.EXTRA_ACCENT_ARGB, colorGrade.accentArgb.toInt())
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reqCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                scheduleExactSafe(alarmManager, triggerAt, pendingIntent)
                newIds.add(reqCode.toString())
            }
        }

        prefs.edit().putStringSet(KEY_IDS, newIds).apply()
    }

    private fun scheduleExactSafe(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        if (canExact) {
            runCatching {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }.onFailure {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}
