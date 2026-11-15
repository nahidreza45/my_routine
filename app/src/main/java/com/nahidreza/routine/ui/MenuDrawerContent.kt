package com.nahidreza.routine.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nahidreza.routine.R
import com.nahidreza.routine.data.RoutineScheduleEngine
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.RoutineDate

private const val RUET_CALENDAR_URL = "https://nahidreza45.github.io/ruet-calendar/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDrawerContent(
    dbState: AppDatabaseState,
    onCloseDrawer: () -> Unit,
    onOpenCtTracker: () -> Unit,
    onUpdateSemesterStart: (String) -> Unit,
    onAdjustWeekOffset: (Int) -> Unit,
    onSavePng: () -> Unit,
    onSharePng: () -> Unit
) {
    val context = LocalContext.current
    var trackerExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val today = remember { RoutineDate.today() }
    val todayAcademicWeek = remember(today, dbState.settings, dbState.holidays) {
        RoutineScheduleEngine.academicWeekFor(today, dbState)
    }
    val todayHoliday = remember(today, dbState.holidays) {
        RoutineScheduleEngine.isHoliday(today, dbState.holidays)
    }
    val startDate = remember(today, dbState.settings.semesterStartDateIso) {
        RoutineDate.fromIso(dbState.settings.semesterStartDateIso)
            ?: today.startOfUniversityWeek()
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.toUtcMidnightMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { ms ->
                            onUpdateSemesterStart(RoutineDate.fromUtcMillis(ms).toIso())
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Save", color = RoutineColors.PrimaryPill)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = RoutineColors.TextSecondary)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    ModalDrawerSheet(
        modifier = Modifier.width(312.dp),
        drawerContainerColor = Color(0xFF0A0C10),
        drawerContentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROUTINE",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    color = RoutineColors.TextPrimary
                )
                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Menu",
                        tint = Color.White.copy(alpha = 0.78f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DrawerSectionLabel("ACADEMIC")

                // Academic Week Card + Expandable Settings
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = RoutineColors.SurfaceCard,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0C3640)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = RoutineColors.CyanAccent,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = "Academic Week",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RoutineColors.TextPrimary
                                    )
                                    Text(
                                        text = todayHoliday?.name ?: "Week ${todayAcademicWeek.coerceAtLeast(1)}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (todayHoliday != null) RoutineColors.AmberWarning else RoutineColors.CyanAccent
                                    )
                                }
                            }

                            IconButton(onClick = { trackerExpanded = !trackerExpanded }) {
                                Icon(
                                    imageVector = if (trackerExpanded) Icons.Rounded.Close else Icons.Rounded.Settings,
                                    contentDescription = "Week Settings",
                                    tint = RoutineColors.TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = trackerExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(color = RoutineColors.BorderSubtle)

                                Text(
                                    text = "SEMESTER START",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp,
                                    color = RoutineColors.TextMuted
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = RoutineColors.SurfaceElevated,
                                    border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePicker = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = startDate.toShortDisplay(),
                                            fontSize = 13.5.sp,
                                            color = RoutineColors.TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.CalendarMonth,
                                            contentDescription = "Pick Date",
                                            tint = RoutineColors.TextSecondary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "WEEK OFFSET",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp,
                                    color = RoutineColors.TextMuted
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = RoutineColors.SurfaceElevated,
                                    border = BorderStroke(1.dp, RoutineColors.BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { onAdjustWeekOffset(-1) }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Remove,
                                                contentDescription = "Decrease Week Offset",
                                                tint = Color.White
                                            )
                                        }
                                        Text(
                                            text = dbState.settings.manualWeekOffset.toString(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoutineColors.CyanAccent
                                        )
                                        IconButton(onClick = { onAdjustWeekOffset(1) }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = "Increase Week Offset",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // CT Marks Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = RoutineColors.SurfaceCard,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .springTouchClickable {
                            onCloseDrawer()
                            onOpenCtTracker()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A2538)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = RoutineColors.PrimaryPill,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Text(
                                text = "CT Marks",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoutineColors.TextPrimary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Best 3",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = RoutineColors.TextMuted
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = RoutineColors.TextMuted
                            )
                        }
                    }
                }

                DrawerSectionLabel("EXPORT")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = RoutineColors.SurfaceCard,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                        modifier = Modifier
                            .weight(1f)
                            .springTouchClickable {
                                onCloseDrawer()
                                onSavePng()
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0C3640)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = RoutineColors.CyanAccent,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Text(
                                text = "Save PNG",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = RoutineColors.TextPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = RoutineColors.SurfaceCard,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                        modifier = Modifier
                            .weight(1f)
                            .springTouchClickable {
                                onCloseDrawer()
                                onSharePng()
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A2538)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    tint = RoutineColors.PrimaryPill,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Share",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = RoutineColors.TextPrimary
                            )
                        }
                    }
                }

                DrawerSectionLabel("LINKS")

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = RoutineColors.SurfaceCard,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .springTouchClickable {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RUET_CALENDAR_URL))
                                context.startActivity(intent)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A2538)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = RoutineColors.PrimaryPill,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Text(
                                text = "RUET Calendar",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoutineColors.TextPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = "Open External Link",
                            tint = RoutineColors.TextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Developer Profile Footer pinned near bottom
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RoutineColors.SurfaceCard,
                border = BorderStroke(1.dp, RoutineColors.SpecularGlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.developer_photo),
                        contentDescription = "Nahid Reza",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, RoutineColors.DrawerAvatarRing, CircleShape)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "DEVELOPER",
                            style = RoutineType.SectionEyebrow,
                            color = RoutineColors.TextMuted
                        )
                        Text(
                            text = "Nahid Reza",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoutineColors.TextPrimary
                        )
                        Text(
                            text = "ECE'23, RUET",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = RoutineColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.1.sp,
        color = RoutineColors.TextMuted,
        modifier = Modifier.padding(top = 4.dp)
    )
}
