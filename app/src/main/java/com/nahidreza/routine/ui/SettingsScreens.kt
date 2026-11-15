package com.nahidreza.routine.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.ColorPalette
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.SubjectColorGrade

@Composable
fun SettingsScreen(
    dbState: AppDatabaseState,
    onSetViewMode: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onChangeLeadMinutes: (Int) -> Unit,
    onManageSubjects: () -> Unit,
    onManageHolidays: () -> Unit,
    onImportBackupUri: (Uri) -> Unit,
    onExportBackupUri: (Uri) -> Unit,
    onMasterReset: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        if (granted) onToggleNotifications(true)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportBackupUri(uri)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExportBackupUri(uri)
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = RoutineColors.SurfaceCard,
            titleContentColor = Color.White,
            textContentColor = RoutineColors.TextSecondary,
            title = { Text("Master Reset", fontWeight = FontWeight.Bold) },
            text = { Text("This will erase all classes, subjects, teachers, CT marks, and holidays. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onMasterReset()
                    }
                ) {
                    Text("Erase All", color = RoutineColors.DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoutineColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Schedule Layout (Day vs Week)
            SettingsSectionHeader("SCHEDULE VIEW")
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = RoutineColors.SurfaceCard,
                border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LayoutOptionTile(
                        title = "Week View",
                        icon = Icons.Rounded.ViewWeek,
                        selected = dbState.settings.viewMode == "WEEK",
                        onClick = { onSetViewMode("WEEK") },
                        modifier = Modifier.weight(1f)
                    )
                    LayoutOptionTile(
                        title = "Day View",
                        icon = Icons.Rounded.ViewDay,
                        selected = dbState.settings.viewMode != "WEEK",
                        onClick = { onSetViewMode("DAY") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Notifications Card
            SettingsSectionHeader("NOTIFICATIONS")
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = RoutineColors.SurfaceCard,
                border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0C3640)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = RoutineColors.CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Class & Lab Reminders",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Alert ${dbState.settings.notificationLeadMinutes} min before start",
                                    fontSize = 12.sp,
                                    color = RoutineColors.TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = dbState.settings.notificationsEnabled && hasNotifPermission,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable && !hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onToggleNotifications(wantEnable)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RoutineColors.PrimaryPillText,
                                checkedTrackColor = RoutineColors.PrimaryPill
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { mins ->
                            val selected = dbState.settings.notificationLeadMinutes == mins
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) RoutineColors.PrimaryPill else RoutineColors.SurfaceElevated,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onChangeLeadMinutes(mins) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) RoutineColors.PrimaryPillText else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Data Management
            SettingsSectionHeader("DATA")
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = RoutineColors.SurfaceCard,
                border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsActionRow(
                        icon = Icons.Rounded.Book,
                        iconBg = Color(0xFF1A2538),
                        iconTint = RoutineColors.PrimaryPill,
                        title = "Manage Subjects",
                        subtitle = "Add, edit, or remove your subjects",
                        onClick = onManageSubjects
                    )
                    HorizontalDivider(color = RoutineColors.BorderSubtle)
                    SettingsActionRow(
                        icon = Icons.Rounded.CalendarMonth,
                        iconBg = Color(0xFF33210F),
                        iconTint = RoutineColors.AmberWarning,
                        title = "Manage Holidays",
                        subtitle = "Set week-based holidays and pause classes",
                        onClick = onManageHolidays
                    )
                    HorizontalDivider(color = RoutineColors.BorderSubtle)
                    SettingsActionRow(
                        icon = Icons.Rounded.Download,
                        iconBg = Color(0xFF0E323D),
                        iconTint = RoutineColors.CyanAccent,
                        title = "Import Backup",
                        subtitle = "Restore data from a JSON file",
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
                    )
                    HorizontalDivider(color = RoutineColors.BorderSubtle)
                    SettingsActionRow(
                        icon = Icons.Rounded.Upload,
                        iconBg = Color(0xFF0F2D1D),
                        iconTint = RoutineColors.EmeraldSuccess,
                        title = "Export Backup",
                        subtitle = "Download your data as a JSON file",
                        onClick = { exportLauncher.launch("routine_backup_${RoutineDate.today().toIso()}.json") }
                    )
                    HorizontalDivider(color = RoutineColors.BorderSubtle)
                    SettingsActionRow(
                        icon = Icons.Rounded.DeleteOutline,
                        iconBg = Color(0xFF361216),
                        iconTint = RoutineColors.DangerRed,
                        title = "Master Reset",
                        titleColor = RoutineColors.DangerRed,
                        subtitle = "Erase all classes, subjects, and teachers",
                        onClick = { showResetDialog = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.1.sp,
        color = RoutineColors.TextMuted,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun LayoutOptionTile(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) RoutineColors.Black else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, RoutineColors.BorderSubtle) else null,
        modifier = modifier.springTouchClickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) RoutineColors.PrimaryPill else RoutineColors.TextMuted,
                modifier = Modifier.size(28.dp)
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = if (selected) RoutineColors.PrimaryPill else Color.Transparent
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) RoutineColors.PrimaryPillText else RoutineColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = RoutineColors.TextSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = RoutineColors.TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun ManageSubjectsScreen(
    dbState: AppDatabaseState,
    onAddSubject: () -> Unit,
    onEditSubject: (String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoutineColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Manage Subjects",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (dbState.subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(28.dp)
                ) {
                    Text(
                        text = "No Subjects Added Yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoutineColors.TextPrimary
                    )
                    Text(
                        text = "Tap Add New Subject below to create your courses and assign colors.",
                        fontSize = 12.5.sp,
                        color = RoutineColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dbState.subjects, key = { it.id }) { subject ->
                    val grade = ColorPalette.byId(subject.colorId)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = RoutineColors.SurfaceCard,
                        border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (subject.isEvenCode) grade.labStart else grade.accent)
                                )
                                Column {
                                    Text(
                                        text = subject.code,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (subject.teachers.isEmpty()) "No teachers assigned" else subject.teachers.joinToString(", "),
                                        fontSize = 13.sp,
                                        color = RoutineColors.TextSecondary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = RoutineColors.SurfaceElevated,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { onEditSubject(subject.id) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "Edit Subject",
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2D1216),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { onDeleteSubject(subject.id) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete Subject",
                                            tint = RoutineColors.DangerRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAddSubject,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RoutineColors.PrimaryPill,
                contentColor = RoutineColors.PrimaryPillText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(54.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Subject", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSubjectScreen(
    dbState: AppDatabaseState,
    subjectId: String?,
    onSave: (subjectId: String?, code: String, colorId: String, teachers: List<String>) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val existing = remember(subjectId, dbState.subjects) {
        dbState.subjects.firstOrNull { it.id == subjectId }
    }
    val usedColorIds = remember(dbState.subjects, subjectId) {
        dbState.subjects.filter { it.id != subjectId }.mapTo(HashSet()) { it.colorId }
    }

    var codeText by remember(existing) { mutableStateOf(existing?.code.orEmpty()) }
    var selectedColorId by remember(existing, dbState.subjects) {
        mutableStateOf(
            existing?.colorId ?: ColorPalette.firstUnusedColorId(dbState.subjects)
        )
    }
    val teachers = remember(existing) {
        mutableStateListOf<String>().apply { addAll(existing?.teachers.orEmpty()) }
    }
    var teacherInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoutineColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
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
                Icon(
                    imageVector = Icons.Rounded.Book,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existing != null) "Edit Subject" else "New Subject",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    val trimmed = codeText.trim()
                    if (trimmed.isNotEmpty() && selectedColorId !in usedColorIds) {
                        onSave(subjectId, trimmed, selectedColorId, teachers.toList())
                        onBack()
                    }
                },
                enabled = codeText.isNotBlank() && selectedColorId !in usedColorIds,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoutineColors.PrimaryPill,
                    contentColor = RoutineColors.PrimaryPillText
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = RoutineColors.SurfaceCard,
                border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "SUBJECT NAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = RoutineColors.PrimaryPill
                    )

                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it.uppercase() },
                        placeholder = { Text("Course code", color = RoutineColors.TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoutineColors.PrimaryPill,
                            unfocusedBorderColor = RoutineColors.BorderSubtle,
                            focusedContainerColor = RoutineColors.Black,
                            unfocusedContainerColor = RoutineColors.Black,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COLOR GRADE (16 PALETTES)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = RoutineColors.PrimaryPill
                        )
                        Text(
                            text = if (selectedColorId in usedColorIds) {
                                "${ColorPalette.byId(selectedColorId).name} (In use)"
                            } else {
                                ColorPalette.byId(selectedColorId).name
                            },
                            fontSize = 11.sp,
                            color = if (selectedColorId in usedColorIds) {
                                RoutineColors.AmberWarning
                            } else {
                                RoutineColors.TextSecondary
                            }
                        )
                    }

                    // Dual-tone Swatch Picker (Theory left half + Deeper Lab right half)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = RoutineColors.Black,
                        border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ColorPalette.grades.forEach { grade ->
                                val isUsed = grade.id in usedColorIds
                                ColorGradeSwatch(
                                    grade = grade,
                                    selected = grade.id == selectedColorId && !isUsed,
                                    usedByOther = isUsed,
                                    onClick = { selectedColorId = grade.id }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = RoutineColors.BorderSubtle)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEACHERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = RoutineColors.PrimaryPill
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RoutineColors.SurfaceElevated
                        ) {
                            Text(
                                text = "Optional",
                                fontSize = 11.sp,
                                color = RoutineColors.TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = teacherInput,
                            onValueChange = { teacherInput = it.uppercase() },
                            placeholder = { Text("Teacher initials", color = RoutineColors.TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoutineColors.PrimaryPill,
                                unfocusedBorderColor = RoutineColors.BorderSubtle,
                                focusedContainerColor = RoutineColors.Black,
                                unfocusedContainerColor = RoutineColors.Black,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = RoutineColors.PrimaryPill,
                            modifier = Modifier
                                .size(50.dp)
                                .clickable {
                                    val clean = teacherInput.trim()
                                    if (clean.isNotEmpty() && clean !in teachers) {
                                        teachers.add(clean)
                                        teacherInput = ""
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add Teacher",
                                    tint = RoutineColors.PrimaryPillText
                                )
                            }
                        }
                    }

                    if (teachers.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            teachers.forEach { t ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = RoutineColors.SurfaceElevated,
                                    border = BorderStroke(1.dp, RoutineColors.BorderSubtle)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = t,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remove",
                                            tint = RoutineColors.TextMuted,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { teachers.remove(t) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorGradeSwatch(
    grade: SubjectColorGrade,
    selected: Boolean,
    usedByOther: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .then(
                if (selected) Modifier.border(2.5.dp, Color.White, CircleShape)
                else Modifier.border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (usedByOther) 0.08f else 0.2f),
                    shape = CircleShape
                )
            )
            .alpha(if (usedByOther) 0.25f else 1f)
            .clickable(enabled = !usedByOther, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(if (selected) 3.dp else 0.dp).clip(CircleShape)) {
            drawArc(
                color = grade.theoryStart,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true
            )
            drawArc(
                color = grade.labStart,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = true
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        } else if (usedByOther) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Already in use",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHolidaysScreen(
    dbState: AppDatabaseState,
    onAddHoliday: (name: String, startIso: String, endIso: String) -> Unit,
    onDeleteHoliday: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showAddSheet by remember { mutableStateOf(false) }
    var holidayName by remember { mutableStateOf("") }
    var startWeekSat by remember { mutableStateOf(RoutineDate.today().startOfUniversityWeek()) }
    var weekCount by remember { mutableIntStateOf(1) }
    var showWeekPicker by remember { mutableStateOf(false) }

    if (showWeekPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = startWeekSat.toUtcMidnightMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showWeekPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { ms ->
                            startWeekSat = RoutineDate.fromUtcMillis(ms).startOfUniversityWeek()
                        }
                        showWeekPicker = false
                    }
                ) {
                    Text("Select Week", color = RoutineColors.PrimaryPill, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeekPicker = false }) {
                    Text("Cancel", color = RoutineColors.TextSecondary)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoutineColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
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
                    text = "Manage Holidays",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = RoutineColors.SurfaceElevated,
                border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = "Week-Based",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RoutineColors.AmberWarning,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = RoutineColors.SurfaceCard,
                border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoutineColors.AmberWarning.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EventBusy,
                            contentDescription = null,
                            tint = RoutineColors.AmberWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Classes pause during holiday weeks and upcoming academic weeks shift automatically so alternating labs stay aligned.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = RoutineColors.TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (dbState.holidays.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "No Holiday Weeks Scheduled",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoutineColors.TextPrimary
                        )
                        Text(
                            text = "Tap Add New Holiday below to pause classes for a full week or longer.",
                            fontSize = 12.5.sp,
                            color = RoutineColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dbState.holidays, key = { it.id }) { h ->
                        val s = RoutineDate.fromIso(h.startDateIso)?.startOfUniversityWeek()
                        val e = RoutineDate.fromIso(h.endDateIso)?.startOfUniversityWeek()
                        val weeks = if (s != null && e != null) {
                            ((s.daysUntil(e) / 7L).toInt() + 1).coerceAtLeast(1)
                        } else 1
                        val endClassWed = e?.plusDays(4)

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = RoutineColors.SurfaceCard,
                            border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = h.name,
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = RoutineColors.AmberWarning.copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = if (weeks == 1) "1 Week" else "$weeks Weeks",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoutineColors.AmberWarning,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (s != null && endClassWed != null) {
                                            "${s.toShortDisplay()} \u2014 ${endClassWed.toShortDisplay()}"
                                        } else {
                                            "${h.startDateIso} to ${h.endDateIso}"
                                        },
                                        fontSize = 12.5.sp,
                                        color = RoutineColors.TextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2D1216),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { onDeleteHoliday(h.id) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete Holiday",
                                            tint = RoutineColors.DangerRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showAddSheet = true },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RoutineColors.PrimaryPill,
                contentColor = RoutineColors.PrimaryPillText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(54.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Holiday", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val endWeekFri = startWeekSat.plusDays(weekCount * 7 - 1)
        val endClassWed = startWeekSat.plusDays((weekCount - 1) * 7 + 4)

        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = RoutineColors.SurfaceDark,
            contentColor = Color.White,
            scrimColor = Color.Black.copy(alpha = 0.55f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "New Holiday Week",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "HOLIDAY NAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = RoutineColors.PrimaryPill
                    )
                    OutlinedTextField(
                        value = holidayName,
                        onValueChange = { holidayName = it },
                        placeholder = { Text("Holiday name", color = RoutineColors.TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoutineColors.PrimaryPill,
                            unfocusedBorderColor = RoutineColors.BorderSubtle,
                            focusedContainerColor = RoutineColors.Black,
                            unfocusedContainerColor = RoutineColors.Black,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "STARTING UNIVERSITY WEEK (SAT \u2014 WED)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = RoutineColors.PrimaryPill
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = RoutineColors.Black,
                        border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWeekPicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Week of ${startWeekSat.toShortDisplay()}",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Text(
                                text = "Pick Date",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoutineColors.PrimaryPill
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DURATION (FULL WEEKS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = RoutineColors.PrimaryPill
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = RoutineColors.Black,
                        border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (weekCount > 1) weekCount-- },
                                enabled = weekCount > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Decrease weeks",
                                    tint = if (weekCount > 1) Color.White else RoutineColors.TextMuted
                                )
                            }
                            Text(
                                text = if (weekCount == 1) "1 Week" else "$weekCount Weeks",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                style = RoutineType.NumericStat,
                                color = RoutineColors.CyanAccent
                            )
                            IconButton(
                                onClick = { if (weekCount < 12) weekCount++ },
                                enabled = weekCount < 12
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Increase weeks",
                                    tint = if (weekCount < 12) Color.White else RoutineColors.TextMuted
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Paused: ${startWeekSat.toShortDisplay()} \u2014 ${endClassWed.toShortDisplay()} (shifts week count by $weekCount)",
                    fontSize = 12.sp,
                    color = RoutineColors.TextSecondary
                )

                Button(
                    onClick = {
                        onAddHoliday(holidayName, startWeekSat.toIso(), endWeekFri.toIso())
                        holidayName = ""
                        weekCount = 1
                        showAddSheet = false
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoutineColors.PrimaryPill,
                        contentColor = RoutineColors.PrimaryPillText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp)
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Holiday", fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
