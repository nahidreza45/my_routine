package com.nahidreza.routine.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.nahidreza.routine.data.RoutineRepository
import com.nahidreza.routine.model.CancelledInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClassAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FIRE_CLASS_ALERT -> {
                val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, 1001)
                val entryId = intent.getStringExtra(EXTRA_ENTRY_ID) ?: return
                val dateIso = intent.getStringExtra(EXTRA_DATE_ISO) ?: return
                val displayCode = intent.getStringExtra(EXTRA_DISPLAY_CODE) ?: "Class"
                val isLab = intent.getBooleanExtra(EXTRA_IS_LAB, false)
                val teacher = intent.getStringExtra(EXTRA_TEACHER).orEmpty()
                val timeRange = intent.getStringExtra(EXTRA_TIME_RANGE).orEmpty()
                val leadMinutes = intent.getIntExtra(EXTRA_LEAD_MINUTES, 10)
                val accentArgb = intent.getIntExtra(EXTRA_ACCENT_ARGB, 0xFF1976D2.toInt())

                NotificationHelper.showClassAlert(
                    context = context,
                    notificationId = notifId,
                    entryId = entryId,
                    dateIso = dateIso,
                    displayCode = displayCode,
                    isLab = isLab,
                    teacher = teacher,
                    timeRange = timeRange,
                    leadMinutes = leadMinutes,
                    accentArgb = accentArgb
                )

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val repo = RoutineRepository(context.applicationContext)
                        val state = repo.stateFlow.first()
                        AlarmScheduler.scheduleUpcomingAlarms(context.applicationContext, state)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_CANCEL_TODAY -> {
                val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)
                val entryId = intent.getStringExtra(EXTRA_ENTRY_ID) ?: return
                val dateIso = intent.getStringExtra(EXTRA_DATE_ISO) ?: return

                if (notifId != -1) {
                    NotificationManagerCompat.from(context).cancel(notifId)
                }

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val repo = RoutineRepository(context.applicationContext)
                        val updated = repo.updateState { current ->
                            val already = current.cancellations.any {
                                it.entryId == entryId && it.dateIso == dateIso
                            }
                            if (already) current else current.copy(
                                cancellations = current.cancellations + CancelledInstance(entryId, dateIso)
                            )
                        }
                        AlarmScheduler.scheduleUpcomingAlarms(context.applicationContext, updated)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_FIRE_CLASS_ALERT = "com.nahidreza.routine.ACTION_FIRE_CLASS_ALERT"
        const val ACTION_CANCEL_TODAY = "com.nahidreza.routine.ACTION_CANCEL_TODAY"

        const val EXTRA_NOTIF_ID = "extra_notif_id"
        const val EXTRA_ENTRY_ID = "extra_entry_id"
        const val EXTRA_DATE_ISO = "extra_date_iso"
        const val EXTRA_DISPLAY_CODE = "extra_display_code"
        const val EXTRA_IS_LAB = "extra_is_lab"
        const val EXTRA_TEACHER = "extra_teacher"
        const val EXTRA_TIME_RANGE = "extra_time_range"
        const val EXTRA_LEAD_MINUTES = "extra_lead_minutes"
        const val EXTRA_ACCENT_ARGB = "extra_accent_argb"
    }
}
