package com.nahidreza.routine.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nahidreza.routine.MainActivity
import com.nahidreza.routine.R

object NotificationHelper {
    const val CHANNEL_ID = "routine_class_alerts"
    private const val CHANNEL_NAME = "Class & Lab Reminders"
    private const val CHANNEL_DESC = "Offline pre-class and pre-lab alerts"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showClassAlert(
        context: Context,
        notificationId: Int,
        entryId: String,
        dateIso: String,
        displayCode: String,
        isLab: Boolean,
        teacher: String,
        timeRange: String,
        leadMinutes: Int,
        accentArgb: Int
    ) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_DATE_ISO, dateIso)
        }
        val openPending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(context, ClassAlarmReceiver::class.java).apply {
            action = ClassAlarmReceiver.ACTION_CANCEL_TODAY
            putExtra(ClassAlarmReceiver.EXTRA_ENTRY_ID, entryId)
            putExtra(ClassAlarmReceiver.EXTRA_DATE_ISO, dateIso)
            putExtra(ClassAlarmReceiver.EXTRA_NOTIF_ID, notificationId)
        }
        val cancelPending = PendingIntent.getBroadcast(
            context,
            notificationId + 50_000,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isLab) {
            "$displayCode (Lab) is about to start"
        } else {
            "$displayCode is about to start"
        }
        val compactRange = timeRange.replace("\u2014", "\u2192")
        val details = buildString {
            append(compactRange)
            if (teacher.isNotBlank()) {
                append(" \u2022 ")
                append(teacher)
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_routine)
            .setColor(accentArgb)
            .setContentTitle(title)
            .setContentText(details)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(0, "Open Schedule", openPending)
            .addAction(0, "Cancel Today", cancelPending)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    const val EXTRA_TARGET_DATE_ISO = "extra_target_date_iso"
}
