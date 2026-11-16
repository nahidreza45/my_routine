package com.nahidreza.routine

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.notification.NotificationHelper
import com.nahidreza.routine.ui.AddEditClassScreen
import com.nahidreza.routine.ui.CtTrackerScreen
import com.nahidreza.routine.ui.EditSubjectScreen
import com.nahidreza.routine.ui.MainScheduleScreen
import com.nahidreza.routine.ui.ManageHolidaysScreen
import com.nahidreza.routine.ui.ManageSubjectsScreen
import com.nahidreza.routine.ui.RoutineRoute
import com.nahidreza.routine.ui.RoutineTheme
import com.nahidreza.routine.ui.RoutineViewModel
import com.nahidreza.routine.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    private val viewModel: RoutineViewModel by viewModels()

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled automatically */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        handleNotificationLaunchIntent(intent)
        askNotificationPermissionIfNeeded()

        setContent {
            val dbState by viewModel.dbState.collectAsStateWithLifecycle()
            val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
            val currentMinutes by viewModel.currentMinutesOfDay.collectAsStateWithLifecycle()
            val route by viewModel.route.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.toastMessage.collect { message ->
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val timeReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        viewModel.syncCurrentTime()
                    }
                }
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_TIME_TICK)
                    addAction(Intent.ACTION_TIME_CHANGED)
                    addAction(Intent.ACTION_TIMEZONE_CHANGED)
                }
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            ContextCompat.registerReceiver(
                                this@MainActivity,
                                timeReceiver,
                                filter,
                                ContextCompat.RECEIVER_NOT_EXPORTED
                            )
                        }
                        Lifecycle.Event.ON_RESUME -> {
                            viewModel.syncCurrentTime()
                        }
                        Lifecycle.Event.ON_STOP -> {
                            runCatching { unregisterReceiver(timeReceiver) }
                        }
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    runCatching { unregisterReceiver(timeReceiver) }
                }
            }

            RoutineTheme {
                AnimatedContent(
                    targetState = route,
                    transitionSpec = {
                        fun depthOf(r: RoutineRoute): Int = when (r) {
                            RoutineRoute.Schedule -> 0
                            is RoutineRoute.EditSubject -> 2
                            else -> 1
                        }
                        val forward = depthOf(targetState) >= depthOf(initialState)
                        if (depthOf(targetState) == depthOf(initialState)) {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                        } else if (forward) {
                            (slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) { it / 7 } +
                                fadeIn(tween(210))) togetherWith
                                (slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { -it / 16 } +
                                    fadeOut(tween(140)))
                        } else {
                            (slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) { -it / 10 } +
                                fadeIn(tween(200))) togetherWith
                                (slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { it / 6 } +
                                    fadeOut(tween(150)))
                        }
                    },
                    label = "routeTransition"
                ) { activeRoute ->
                    when (activeRoute) {
                        is RoutineRoute.Schedule -> {
                            MainScheduleScreen(
                                dbState = dbState,
                                selectedDate = selectedDate,
                                currentMinutesOfDay = currentMinutes,
                                onSelectDate = viewModel::selectDate,
                                onJumpToUpNext = { viewModel.jumpToUpNext() },
                                onOpenAddClass = { day, slot, parity ->
                                    viewModel.navigateTo(
                                        RoutineRoute.AddEditClass(
                                            editEntryId = null,
                                            prefillDay = day,
                                            prefillSlot = slot,
                                            targetParity = parity
                                        )
                                    )
                                },
                                onOpenEditClass = { entryId, parity ->
                                    viewModel.navigateTo(
                                        RoutineRoute.AddEditClass(
                                            editEntryId = entryId,
                                            targetParity = parity
                                        )
                                    )
                                },
                                onToggleCancel = viewModel::toggleCancelForDate,
                                onShiftLabNextWeek = viewModel::shiftLabNextWeek,
                                onSwapTheory = { resolved, dir ->
                                    viewModel.swapTheoryClass(resolved.entry, resolved.academicWeek, dir)
                                },
                                onDeleteEntry = viewModel::deleteRoutineEntry,
                                onOpenSettings = { viewModel.navigateTo(RoutineRoute.Settings) },
                                onOpenCtTracker = { viewModel.navigateTo(RoutineRoute.CtTracker) },
                                onUpdateSemesterStart = viewModel::updateSemesterStart,
                                onAdjustWeekOffset = viewModel::adjustManualWeekOffset,
                                onSavePng = viewModel::saveRoutinePng,
                                onSharePng = {
                                    viewModel.shareRoutinePng { chooser ->
                                        startActivity(chooser)
                                    }
                                }
                            )
                        }

                        is RoutineRoute.AddEditClass -> {
                            AddEditClassScreen(
                                dbState = dbState,
                                editEntryId = activeRoute.editEntryId,
                                prefillDay = activeRoute.prefillDay,
                                prefillSlot = activeRoute.prefillSlot,
                                initialParity = activeRoute.targetParity,
                                onSave = viewModel::saveRoutineEntry,
                                onManageSubjects = { viewModel.navigateTo(RoutineRoute.ManageSubjects) },
                                onBack = viewModel::navigateBackToSchedule
                            )
                        }

                        is RoutineRoute.CtTracker -> {
                            CtTrackerScreen(
                                dbState = dbState,
                                onUpdateMark = viewModel::updateCtMark,
                                onBack = viewModel::navigateBackToSchedule
                            )
                        }

                        is RoutineRoute.Settings -> {
                            SettingsScreen(
                                dbState = dbState,
                                onSetViewMode = viewModel::setViewMode,
                                onToggleNotifications = viewModel::updateNotificationsEnabled,
                                onChangeLeadMinutes = viewModel::updateNotificationLeadMinutes,
                                onManageSubjects = { viewModel.navigateTo(RoutineRoute.ManageSubjects) },
                                onManageHolidays = { viewModel.navigateTo(RoutineRoute.ManageHolidays) },
                                onImportBackupUri = viewModel::importBackupUri,
                                onExportBackupUri = viewModel::exportBackupUri,
                                onMasterReset = viewModel::masterReset,
                                onBack = viewModel::navigateBackToSchedule
                            )
                        }

                        is RoutineRoute.ManageSubjects -> {
                            ManageSubjectsScreen(
                                dbState = dbState,
                                onAddSubject = { viewModel.navigateTo(RoutineRoute.EditSubject(null)) },
                                onEditSubject = { id -> viewModel.navigateTo(RoutineRoute.EditSubject(id)) },
                                onDeleteSubject = viewModel::deleteSubject,
                                onBack = { viewModel.navigateTo(RoutineRoute.Settings) }
                            )
                        }

                        is RoutineRoute.EditSubject -> {
                            EditSubjectScreen(
                                dbState = dbState,
                                subjectId = activeRoute.subjectId,
                                onSave = viewModel::saveSubject,
                                onBack = { viewModel.navigateTo(RoutineRoute.ManageSubjects) }
                            )
                        }

                        is RoutineRoute.ManageHolidays -> {
                            ManageHolidaysScreen(
                                dbState = dbState,
                                onAddHoliday = viewModel::addHoliday,
                                onDeleteHoliday = viewModel::deleteHoliday,
                                onBack = { viewModel.navigateTo(RoutineRoute.Settings) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationLaunchIntent(intent)
    }

    private fun handleNotificationLaunchIntent(intent: Intent?) {
        val targetIso = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_DATE_ISO) ?: return
        RoutineDate.fromIso(targetIso)?.let { date ->
            viewModel.selectDateFromNotification(date)
        }
    }

    private fun askNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
