package com.nahidreza.routine.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nahidreza.routine.data.RoutineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
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
    }
}
