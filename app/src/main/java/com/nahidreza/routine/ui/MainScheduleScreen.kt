package com.nahidreza.routine.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.tanh
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nahidreza.routine.data.ResolvedEntry
import com.nahidreza.routine.data.RoutineScheduleEngine
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.ColorPalette
import com.nahidreza.routine.model.RepeatPattern
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.ScheduleSlots
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val TOTAL_DAY_PAGES = 10_000
private const val INITIAL_DAY_PAGE = 5_000
private const val TOTAL_WEEK_PAGES = 2_000
private const val INITIAL_WEEK_PAGE = 1_000

private const val WEEK_START_MINUTE = 8 * 60
private const val WEEK_END_MINUTE = 17 * 60
private const val WEEK_DP_PER_MIN = 1.68f

private val WEEK_SLOT_HEIGHT = (WEEK_DP_PER_MIN * 50f).dp
private val WEEK_TEA_BREAK_HEIGHT = (WEEK_DP_PER_MIN * 20f).dp
private val WEEK_LUNCH_BREAK_HEIGHT = (WEEK_DP_PER_MIN * 70f).dp
private val WEEK_AXIS_WIDTH = 56.dp
private val WEEK_TOP_PADDING = 14.dp
private val WEEK_END_PADDING = 8.dp
private val WEEK_AXIS_STATIC_COLOR = Color(0xFFB0BBCF)

private val FabSurfaceBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF202A3E), Color(0xFF121824))
)
private val FabBorderBrush = Brush.verticalGradient(
    colors = listOf(RoutineColors.PrimaryPill.copy(alpha = 0.55f), Color.White.copy(alpha = 0.12f))
)
private val BreakStripBrush = Brush.horizontalGradient(
    colors = listOf(Color(0xFF121620), Color(0xFF181D29), Color(0xFF121620))
)
private val SheetTopBorderBrush = Brush.verticalGradient(
    colors = listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))
)

@Composable
fun MainScheduleScreen(
    dbState: AppDatabaseState,
    selectedDate: RoutineDate,
    currentMinutesOfDay: Int,
    onSelectDate: (RoutineDate) -> Unit,
    onJumpToUpNext: () -> Unit,
    onOpenAddClass: (prefillDay: Int, prefillSlot: Int?, targetParity: Int) -> Unit,
    onOpenEditClass: (entryId: String, targetParity: Int) -> Unit,
    onToggleCancel: (entryId: String, date: RoutineDate) -> Unit,
    onShiftLabNextWeek: (entryId: String, academicWeek: Int) -> Unit,
    onSwapTheory: (ResolvedEntry, Int) -> Unit,
    onDeleteEntry: (entryId: String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCtTracker: () -> Unit,
    onUpdateSemesterStart: (String) -> Unit,
    onAdjustWeekOffset: (Int) -> Unit,
    onSavePng: () -> Unit,
    onSharePng: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var activeSheetEntry by remember { mutableStateOf<ResolvedEntry?>(null) }

    val today = remember(currentMinutesOfDay) { RoutineDate.today() }
    val anchorWeekStart = remember(today) { today.startOfUniversityWeek() }
    val isWeekMode = dbState.settings.viewMode == "WEEK"

    val upNextTarget = remember(dbState, today, currentMinutesOfDay) {
        RoutineScheduleEngine.findNextClassTarget(
            state = dbState,
            nowDate = today,
            nowMinutesOfDay = currentMinutesOfDay
        )
    }

    if (activeSheetEntry != null) {
        BackHandler { activeSheetEntry = null }
    } else if (drawerState.isOpen) {
        BackHandler { scope.launch { drawerState.close() } }
    }

    val isOverlayOpen by remember {
        derivedStateOf { drawerState.targetValue == DrawerValue.Open || activeSheetEntry != null }
    }
    val contentScale by animateFloatAsState(
        targetValue = if (isOverlayOpen) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "overlayDepthScale"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        drawerContent = {
            MenuDrawerContent(
                dbState = dbState,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onOpenCtTracker = onOpenCtTracker,
                onUpdateSemesterStart = onUpdateSemesterStart,
                onAdjustWeekOffset = onAdjustWeekOffset,
                onSavePng = onSavePng,
                onSharePng = onSharePng
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RoutineColors.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = contentScale
                        scaleY = contentScale
                    }
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                ScheduleTopBar(
                    monthTitle = selectedDate.monthName,
                    onOpenMenu = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch { drawerState.open() }
                    },
                    onJumpToUpNext = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onJumpToUpNext()
                    },
                    onOpenSettings = onOpenSettings
                )

                val weekStart = remember(selectedDate) { selectedDate.startOfUniversityWeek() }
                DaySelectorStrip(
                    weekStart = weekStart,
                    today = today,
                    selectedDate = selectedDate,
                    isWeekMode = isWeekMode,
                    onSelectDay = { date ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectDate(date)
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                Box(modifier = Modifier.weight(1f)) {
                    if (!isWeekMode) {
                        DayViewPager(
                            anchorWeekStart = anchorWeekStart,
                            selectedDate = selectedDate,
                            dbState = dbState,
                            currentMinutesOfDay = currentMinutesOfDay,
                            upNextTarget = upNextTarget,
                            onSelectDate = onSelectDate,
                            onTapCard = { activeSheetEntry = it }
                        )
                    } else {
                        WeekViewPager(
                            anchorWeekStart = anchorWeekStart,
                            selectedDate = selectedDate,
                            dbState = dbState,
                            currentMinutesOfDay = currentMinutesOfDay,
                            upNextTarget = upNextTarget,
                            onSelectDate = onSelectDate,
                            onTapCard = { activeSheetEntry = it },
                            onTapEmptySlot = { dayOfWeek, slot, academicWeek ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOpenAddClass(
                                    dayOfWeek,
                                    slot,
                                    RepeatPattern.parityForAcademicWeek(academicWeek)
                                )
                            }
                        )
                    }
                }
            }

            // Elevated Squircle + Action Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 18.dp, bottom = 20.dp)
                    .size(56.dp)
                    .springTouchClickable {
                        val academicWeek = RoutineScheduleEngine.academicWeekFor(selectedDate, dbState)
                        onOpenAddClass(
                            selectedDate.satBasedDayOfWeek.coerceIn(0, 4),
                            null,
                            RepeatPattern.parityForAcademicWeek(academicWeek)
                        )
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(FabSurfaceBrush)
                    .border(1.dp, FabBorderBrush, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Class",
                    tint = RoutineColors.PrimaryPill,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Scrim + Bottom Sheet
            AnimatedVisibility(
                visible = activeSheetEntry != null,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(160))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.62f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { activeSheetEntry = null }
                        )
                )
            }

            AnimatedVisibility(
                visible = activeSheetEntry != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(tween(160)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(160)
                ) + fadeOut(tween(140)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                activeSheetEntry?.let { resolved ->
                    ClassActionBottomSheet(
                        resolved = resolved,
                        dbState = dbState,
                        onDismiss = { activeSheetEntry = null },
                        onEdit = {
                            activeSheetEntry = null
                            onOpenEditClass(
                                resolved.entry.id,
                                RepeatPattern.parityForAcademicWeek(resolved.academicWeek)
                            )
                        },
                        onToggleCancel = {
                            activeSheetEntry = null
                            onToggleCancel(resolved.entry.id, resolved.date)
                        },
                        onShiftNextWeek = {
                            activeSheetEntry = null
                            onShiftLabNextWeek(resolved.entry.id, resolved.academicWeek)
                        },
                        onSwap = { dir ->
                            activeSheetEntry = null
                            onSwapTheory(resolved, dir)
                        },
                        onDelete = {
                            activeSheetEntry = null
                            onDeleteEntry(resolved.entry.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTopBar(
    monthTitle: String,
    onOpenMenu: () -> Unit,
    onJumpToUpNext: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenMenu),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Open Menu",
                    tint = RoutineColors.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            AnimatedContent(
                targetState = monthTitle,
                transitionSpec = {
                    (fadeIn(tween(180)) + slideInVertically(tween(180)) { -it / 4 }) togetherWith
                        fadeOut(tween(140))
                },
                label = "monthTitleFade"
            ) { title ->
                Text(
                    text = title,
                    style = RoutineType.TopBarMonth,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset(y = (-0.5).dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cyan Up-Next Action Pill
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(RoutineColors.CyanAccent.copy(alpha = 0.14f))
                    .border(1.dp, RoutineColors.CyanAccent.copy(alpha = 0.36f), CircleShape)
                    .clickable(onClick = onJumpToUpNext),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FastForward,
                    contentDescription = "Jump to Up Next",
                    tint = RoutineColors.CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Settings",
                    tint = RoutineColors.TextPrimary.copy(alpha = 0.88f)
                )
            }
        }
    }
}

@Composable
private fun DaySelectorStrip(
    weekStart: RoutineDate,
    today: RoutineDate,
    selectedDate: RoutineDate,
    isWeekMode: Boolean,
    onSelectDay: (RoutineDate) -> Unit
) {
    val startPad by animateDpAsState(
        targetValue = if (isWeekMode) WEEK_AXIS_WIDTH else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "stripStartPad"
    )
    val endPad by animateDpAsState(
        targetValue = if (isWeekMode) WEEK_END_PADDING else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "stripEndPad"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPad, end = endPad, top = 6.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        for (i in 0..4) {
            val date = weekStart.plusDays(i)
            val isToday = date == today
            val isSelected = !isWeekMode && date == selectedDate

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelectDay(date) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isToday) RoutineColors.CyanAccent else Color.Transparent)
                )
                Text(
                    text = date.shortDayName,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = when {
                        isSelected -> RoutineColors.TextPrimary
                        isToday -> RoutineColors.CyanAccent
                        else -> RoutineColors.TextMuted
                    }
                )
                val pillShape = RoundedCornerShape(12.dp)
                val pillBgColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> RoutineColors.PrimaryPill
                        isWeekMode && isToday -> RoutineColors.SurfaceCard
                        isWeekMode -> Color.Transparent
                        else -> RoutineColors.SurfaceCard
                    },
                    animationSpec = tween(180),
                    label = "dayPillBg"
                )
                val dayNumColor by animateColorAsState(
                    targetValue = if (isSelected) RoutineColors.PrimaryPillText else RoutineColors.TextPrimary.copy(alpha = 0.9f),
                    animationSpec = tween(180),
                    label = "dayNumColor"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isWeekMode) 3.dp else 5.dp)
                        .widthIn(max = 48.dp)
                        .height(36.dp)
                        .clip(pillShape)
                        .background(pillBgColor)
                        .then(
                            when {
                                isSelected -> Modifier
                                isToday -> Modifier.border(1.dp, RoutineColors.CyanAccent.copy(alpha = 0.4f), pillShape)
                                !isWeekMode -> Modifier.border(1.dp, RoutineColors.SpecularGlassBorder, pillShape)
                                else -> Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.day.toString(),
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        style = RoutineType.NumericStat,
                        color = dayNumColor
                    )
                }
            }
        }
    }
}

@Composable
private fun DayViewPager(
    anchorWeekStart: RoutineDate,
    selectedDate: RoutineDate,
    dbState: AppDatabaseState,
    currentMinutesOfDay: Int,
    upNextTarget: Pair<RoutineDate, String>?,
    onSelectDate: (RoutineDate) -> Unit,
    onTapCard: (ResolvedEntry) -> Unit
) {
    fun dateToPage(d: RoutineDate): Int {
        val snapped = RoutineDate.nearestActiveOrNextWorkingDate(d)
        val weekDiff = Math.floorDiv(anchorWeekStart.daysUntil(snapped.startOfUniversityWeek()), 7L).toInt()
        return (INITIAL_DAY_PAGE + weekDiff * 5 + snapped.satBasedDayOfWeek).coerceIn(0, TOTAL_DAY_PAGES - 1)
    }

    fun pageToDate(page: Int): RoutineDate {
        val delta = page - INITIAL_DAY_PAGE
        val weekDiff = Math.floorDiv(delta, 5)
        val dayOfWeek = Math.floorMod(delta, 5)
        return anchorWeekStart.plusDays(weekDiff * 7 + dayOfWeek)
    }

    val pagerState = rememberPagerState(
        initialPage = dateToPage(selectedDate),
        pageCount = { TOTAL_DAY_PAGES }
    )
    val latestSelectedDate by rememberUpdatedState(selectedDate)
    val latestOnSelectDate by rememberUpdatedState(onSelectDate)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val newDate = pageToDate(page)
                if (newDate != latestSelectedDate) {
                    latestOnSelectDate(newDate)
                }
            }
    }

    LaunchedEffect(selectedDate) {
        val targetPage = dateToPage(selectedDate)
        if (!pagerState.isScrollInProgress && pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val pageDate = remember(page, anchorWeekStart) { pageToDate(page) }
        val holiday = remember(pageDate, dbState.holidays) {
            RoutineScheduleEngine.isHoliday(pageDate, dbState.holidays)
        }
        val entries = remember(pageDate, dbState, currentMinutesOfDay, upNextTarget) {
            RoutineScheduleEngine.entriesForDate(
                date = pageDate,
                state = dbState,
                liveNowMinutes = currentMinutesOfDay,
                upNextTarget = upNextTarget
            )
        }

        if (holiday != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = RoutineColors.SurfaceCard,
                    border = BorderStroke(1.dp, RoutineColors.BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(RoutineColors.AmberWarning.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EventBusy,
                                contentDescription = null,
                                tint = RoutineColors.AmberWarning,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text(
                            text = holiday.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoutineColors.TextPrimary
                        )
                        Text(
                            text = "Classes are paused for this holiday.",
                            fontSize = 13.sp,
                            color = RoutineColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "No Classes Today",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoutineColors.TextSecondary
                    )
                    Text(
                        text = "Tap + to schedule a session on ${pageDate.fullDayName}",
                        fontSize = 12.5.sp,
                        color = RoutineColors.TextMuted
                    )
                }
            }
        } else {
            val density = LocalDensity.current
            val scope = rememberCoroutineScope()
            val elasticOffset = remember { Animatable(0f) }
            val maxPullPx = remember(density) { with(density) { 84.dp.toPx() } }
            val maxBouncePx = remember(density) { with(density) { 26.dp.toPx() } }
            val maxRawPullPx = remember(maxPullPx) { maxPullPx * 4.5f }
            val pullHolder = remember { floatArrayOf(0f) }

            fun rubberBand(raw: Float): Float {
                if (raw == 0f) return 0f
                return (maxPullPx * tanh(abs(raw) / (maxPullPx * 2.1f)) * sign(raw))
            }

            val elasticConnection = remember(maxPullPx, maxBouncePx, maxRawPullPx) {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (source != NestedScrollSource.UserInput || available.y == 0f) return Offset.Zero
                        val raw = pullHolder[0]
                        if (raw > 0f && available.y < 0f) {
                            val consumedY = maxOf(available.y, -raw)
                            pullHolder[0] = raw + consumedY
                            scope.launch { elasticOffset.snapTo(rubberBand(pullHolder[0])) }
                            return Offset(0f, consumedY)
                        } else if (raw < 0f && available.y > 0f) {
                            val consumedY = minOf(available.y, -raw)
                            pullHolder[0] = raw + consumedY
                            scope.launch { elasticOffset.snapTo(rubberBand(pullHolder[0])) }
                            return Offset(0f, consumedY)
                        }
                        return Offset.Zero
                    }

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        if (source != NestedScrollSource.UserInput || available.y == 0f) return Offset.Zero
                        pullHolder[0] = (pullHolder[0] + available.y).coerceIn(-maxRawPullPx, maxRawPullPx)
                        scope.launch { elasticOffset.snapTo(rubberBand(pullHolder[0])) }
                        return Offset(0f, available.y)
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (pullHolder[0] != 0f) {
                            pullHolder[0] = 0f
                            elasticOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.72f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            return Velocity(0f, available.y)
                        }
                        return Velocity.Zero
                    }

                    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                        pullHolder[0] = 0f
                        if (elasticOffset.value != 0f) {
                            elasticOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.72f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        } else if (abs(available.y) > 250f) {
                            val impulse = (available.y * 0.012f).coerceIn(-maxBouncePx, maxBouncePx)
                            elasticOffset.snapTo(impulse)
                            elasticOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.68f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                        return Velocity.Zero
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(elasticConnection),
                overscrollEffect = null,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = entries,
                    key = { _, item -> "${item.entry.id}_${item.date.toIso()}" },
                    contentType = { _, _ -> "dayCard" }
                ) { index, item ->
                    DayRoutineCard(
                        item = item,
                        currentMinutesOfDay = currentMinutesOfDay,
                        onClick = { onTapCard(item) },
                        modifier = Modifier
                            .graphicsLayer {
                                val off = elasticOffset.value
                                translationY = when {
                                    off > 0f -> off + index * (off * 0.10f)
                                    off < 0f -> off - (entries.lastIndex - index) * (-off * 0.08f)
                                    else -> 0f
                                }
                            }
                            .animateItem(
                                fadeInSpec = tween(200),
                                fadeOutSpec = tween(160),
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun DayRoutineCard(
    item: ResolvedEntry,
    currentMinutesOfDay: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grade = remember(item.subject.colorId) { ColorPalette.byId(item.subject.colorId) }
    val isDeeperTone = item.entry.isLab || item.subject.isEvenCode
    val cardAlpha by animateFloatAsState(
        targetValue = if (item.isCancelled) 0.48f else 1f,
        animationSpec = tween(220),
        label = "dayCardAlpha"
    )
    val timeLabel = remember(item.entry.startSlot, item.entry.span) {
        ScheduleSlots.timeRangeLabel(item.entry.startSlot, item.entry.span)
    }
    val startSlotSpec = ScheduleSlots.slots.getOrNull(
        item.entry.startSlot.coerceIn(0, ScheduleSlots.slots.lastIndex)
    )
    val endSlotSpec = ScheduleSlots.slots.getOrNull(
        (item.entry.startSlot + item.entry.span - 1).coerceIn(0, ScheduleSlots.slots.lastIndex)
    )
    val startMin = startSlotSpec?.startMinutesOfDay ?: 0
    val endMin = endSlotSpec?.endMinutesOfDay ?: startMin
    val liveProgress = if (item.isLiveNow && endMin > startMin) {
        ((currentMinutesOfDay - startMin).toFloat() / (endMin - startMin).toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val remainingLabel = if (item.isLiveNow) {
        val rem = (endMin - currentMinutesOfDay).coerceAtLeast(1)
        val hrs = rem / 60
        val mins = rem % 60
        when {
            hrs > 0 && mins > 0 -> "${hrs}h ${mins}m left"
            hrs > 0 -> "${hrs}h left"
            else -> "${mins}m left"
        }
    } else {
        null
    }
    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .springTouchClickable(onClick = onClick)
            .graphicsLayer { alpha = cardAlpha }
            .clip(cardShape)
            .background(grade.gradientBrush(isLabTone = isDeeperTone))
            .background(RoutineColors.CardSheenOverlay)
            .then(
                if (!item.isLiveNow) Modifier.border(1.dp, RoutineColors.SpecularCardBorder, cardShape)
                else Modifier
            )
    ) {
        if (item.isLiveNow) {
            LiveCardAuraOverlay(
                cornerRadius = 22.dp,
                modifier = Modifier.matchParentSize()
            )
        }
        // Status Badge anchored cleanly in top-right corner
        if (item.isCancelled) {
            StatusCornerPill(
                text = "CANCELLED",
                textColor = Color.White,
                backgroundColor = Color.Black.copy(alpha = 0.42f),
                borderColor = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 15.dp, end = 16.dp)
            )
        } else if (item.isLiveNow) {
            LiveTimerOvalBadge(
                progress = liveProgress,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 15.dp, end = 16.dp)
            )
        } else if (item.isUpNext) {
            StatusCornerPill(
                text = "UP NEXT",
                textColor = Color.White,
                backgroundColor = Color.Black.copy(alpha = 0.36f),
                borderColor = Color.White.copy(alpha = 0.22f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 15.dp, end = 16.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 17.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Course code & metadata pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = if (item.isCancelled || item.isLiveNow || item.isUpNext) 88.dp else 0.dp)
            ) {
                Text(
                    text = item.displayCode,
                    style = RoutineType.DayHeroCode,
                    maxLines = 1
                )

                if (item.entry.teacher.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.entry.teacher,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                if (item.entry.isLab) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.34f))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LAB",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.7.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Time capsule + duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = timeLabel,
                        style = RoutineType.DayTimePill
                    )
                }

                if (remainingLabel != null) {
                    Text(
                        text = remainingLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = RoutineType.NumericStat,
                        color = Color.White.copy(alpha = 0.86f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCornerPill(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.9.sp,
            color = textColor
        )
    }
}

@Composable
private fun LiveTimerOvalBadge(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nowOvalProgress"
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .width(56.dp)
            .height(24.dp)
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.34f))
            .border(0.9.dp, Color.White.copy(alpha = 0.34f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.28f))
            )
        }
        Text(
            text = "NOW",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.9.sp,
            color = Color.White
        )
    }
}

@Composable
private fun LiveCardAuraOverlay(
    cornerRadius: Dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "liveCardAura")
    val orbitAngleDeg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderOrbitAngle"
    )
    val shimmerProgress by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diagonalSilkShimmer"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // 1. Wide feathered diagonal silk shimmer (no hard edge behind pills/text)
        val bandHalfWidth = w * 0.42f
        val centerX = w * shimmerProgress
        drawRect(
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to Color.Transparent,
                    0.32f to Color.White.copy(alpha = 0.024f),
                    0.50f to Color.White.copy(alpha = 0.070f),
                    0.68f to Color.White.copy(alpha = 0.024f),
                    1.00f to Color.Transparent
                ),
                start = Offset(centerX - bandHalfWidth, 0f),
                end = Offset(centerX + bandHalfWidth, h)
            )
        )

        // 2. Orbiting luminous specular border along rounded perimeter
        val strokePx = 1.6.dp.toPx()
        val inset = strokePx / 2f
        val rPx = (cornerRadius.toPx() - inset).coerceAtLeast(0f)
        val cx = w / 2f
        val cy = h / 2f
        val radius = hypot(cx, cy)
        val rad = orbitAngleDeg * (PI / 180.0)
        val dx = (cos(rad) * radius).toFloat()
        val dy = (sin(rad) * radius).toFloat()

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.14f),
                    RoutineColors.CyanAccent.copy(alpha = 0.85f),
                    Color.White.copy(alpha = 0.92f),
                    RoutineColors.CyanAccent.copy(alpha = 0.42f),
                    Color.White.copy(alpha = 0.14f)
                ),
                start = Offset(cx - dx, cy - dy),
                end = Offset(cx + dx, cy + dy)
            ),
            topLeft = Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(
                (w - strokePx).coerceAtLeast(0f),
                (h - strokePx).coerceAtLeast(0f)
            ),
            cornerRadius = CornerRadius(rPx, rPx),
            style = Stroke(width = strokePx)
        )
    }
}

@Composable
private fun WeekViewPager(
    anchorWeekStart: RoutineDate,
    selectedDate: RoutineDate,
    dbState: AppDatabaseState,
    currentMinutesOfDay: Int,
    upNextTarget: Pair<RoutineDate, String>?,
    onSelectDate: (RoutineDate) -> Unit,
    onTapCard: (ResolvedEntry) -> Unit,
    onTapEmptySlot: (dayOfWeek: Int, slot: Int, academicWeek: Int) -> Unit
) {
    fun dateToWeekPage(d: RoutineDate): Int {
        val diff = Math.floorDiv(anchorWeekStart.daysUntil(d.startOfUniversityWeek()), 7L).toInt()
        return (INITIAL_WEEK_PAGE + diff).coerceIn(0, TOTAL_WEEK_PAGES - 1)
    }

    fun pageToWeekStart(page: Int): RoutineDate {
        return anchorWeekStart.plusDays((page - INITIAL_WEEK_PAGE) * 7)
    }

    val pagerState = rememberPagerState(
        initialPage = dateToWeekPage(selectedDate),
        pageCount = { TOTAL_WEEK_PAGES }
    )
    val latestSelectedDate by rememberUpdatedState(selectedDate)
    val latestOnSelectDate by rememberUpdatedState(onSelectDate)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val newStart = pageToWeekStart(page)
                if (newStart != latestSelectedDate.startOfUniversityWeek()) {
                    val dayIdx = latestSelectedDate.satBasedDayOfWeek.coerceIn(0, 4)
                    latestOnSelectDate(newStart.plusDays(dayIdx))
                }
            }
    }

    LaunchedEffect(selectedDate) {
        val target = dateToWeekPage(selectedDate)
        if (!pagerState.isScrollInProgress && pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    val slotTops = remember {
        ScheduleSlots.slots.map { spec ->
            WEEK_TOP_PADDING + (WEEK_DP_PER_MIN * (spec.startMinutesOfDay - WEEK_START_MINUTE)).dp
        }
    }
    val totalHeight = remember {
        WEEK_TOP_PADDING + (WEEK_DP_PER_MIN * (WEEK_END_MINUTE - WEEK_START_MINUTE)).dp + 28.dp
    }
    val density = LocalDensity.current
    val slotTopsPx = remember(density, slotTops) {
        with(density) { slotTops.map { it.toPx() } }
    }
    val topPadPx = remember(density) { with(density) { WEEK_TOP_PADDING.toPx() } }
    val endPadPx = remember(density) { with(density) { WEEK_END_PADDING.toPx() } }
    val slotHeightPx = remember(density) { with(density) { WEEK_SLOT_HEIGHT.toPx() } }
    val axisWidthPx = remember(density) { with(density) { WEEK_AXIS_WIDTH.toPx() } }

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val scrollState = rememberScrollState()
        val weekStart = remember(page, anchorWeekStart) { pageToWeekStart(page) }
        val weekDates = remember(weekStart) { (0..4).map { weekStart.plusDays(it) } }
        val academicWeek = remember(weekStart, dbState.settings, dbState.holidays) {
            RoutineScheduleEngine.academicWeekFor(weekStart, dbState)
        }
        val weekHoliday = remember(weekDates, dbState.holidays) {
            RoutineScheduleEngine.holidayForWeek(weekDates, dbState.holidays)
        }
        val entriesByDay = remember(weekDates, dbState, currentMinutesOfDay, upNextTarget) {
            weekDates.map { d ->
                RoutineScheduleEngine.entriesForDate(
                    date = d,
                    state = dbState,
                    liveNowMinutes = currentMinutesOfDay,
                    upNextTarget = upNextTarget
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 84.dp)
        ) {
            val colWidth = (maxWidth - WEEK_AXIS_WIDTH - WEEK_END_PADDING) / 5f
            val colWidthPx = with(density) { colWidth.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight)
                    .pointerInput(academicWeek, weekHoliday, colWidthPx) {
                        detectTapGestures { tapOffset ->
                            if (weekHoliday != null) return@detectTapGestures
                            val gridX = tapOffset.x - axisWidthPx
                            if (gridX < 0f || colWidthPx <= 0f) return@detectTapGestures
                            val dayIdx = (gridX / colWidthPx).toInt().coerceIn(0, 4)
                            val y = tapOffset.y
                            for (slotIdx in 0..8) {
                                val top = slotTopsPx[slotIdx]
                                if (y >= top && y < top + slotHeightPx) {
                                    onTapEmptySlot(dayIdx, slotIdx, academicWeek)
                                    break
                                }
                            }
                        }
                    }
                    .drawBehind {
                        val gridRight = size.width - endPadPx
                        val gridWidth = (gridRight - axisWidthPx).coerceAtLeast(0f)
                        val gridBottom = slotTopsPx[8] + slotHeightPx
                        val gridLineColor = Color(0x0EFFFFFF)
                        val strokeW = 0.8.dp.toPx()

                        // Vertical day column separators
                        for (c in 0..5) {
                            val x = axisWidthPx + colWidthPx * c
                            drawLine(
                                color = gridLineColor,
                                start = Offset(x, topPadPx),
                                end = Offset(x, gridBottom),
                                strokeWidth = strokeW
                            )
                        }
                        // Horizontal slot borders
                        for (i in 0..8) {
                            val top = slotTopsPx[i]
                            drawLine(
                                color = gridLineColor,
                                start = Offset(axisWidthPx, top),
                                end = Offset(axisWidthPx + gridWidth, top),
                                strokeWidth = strokeW
                            )
                            val bottom = top + slotHeightPx
                            drawLine(
                                color = gridLineColor,
                                start = Offset(axisWidthPx, bottom),
                                end = Offset(axisWidthPx + gridWidth, bottom),
                                strokeWidth = strokeW
                            )
                        }
                    }
            ) {
                // Proportional time axis labels + relative-height break bands
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(WEEK_TOP_PADDING))
                    for (i in 0..8) {
                        if (i == 3) {
                            WeekBreakRow(
                                timeLabel = "10:30 am",
                                title = "TEA BREAK",
                                subtitle = null,
                                breakHeight = WEEK_TEA_BREAK_HEIGHT
                            )
                        } else if (i == 6) {
                            WeekBreakRow(
                                timeLabel = "1:20 pm",
                                title = "LUNCH & PRAYER BREAK",
                                subtitle = "1:20 \u2014 2:30 PM",
                                breakHeight = WEEK_LUNCH_BREAK_HEIGHT
                            )
                        }
                        val slotSpec = ScheduleSlots.slots[i]
                        Box(
                            modifier = Modifier
                                .width(WEEK_AXIS_WIDTH)
                                .height(WEEK_SLOT_HEIGHT)
                                .padding(end = 6.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Text(
                                text = formatSlotAxisAmPm(slotSpec.startHour24, slotSpec.startMinute),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Medium,
                                style = RoutineType.NumericStat,
                                color = WEEK_AXIS_STATIC_COLOR,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.offset(y = (-6).dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(WEEK_AXIS_WIDTH)
                            .padding(end = 6.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = "5 pm",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            style = RoutineType.NumericStat,
                            color = WEEK_AXIS_STATIC_COLOR,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.offset(y = (-6).dp)
                        )
                    }
                }

                // Class & Lab Cards overlaid at exact proportional height
                entriesByDay.forEachIndexed { dayIdx, list ->
                    list.forEach { item ->
                        val startSpec = ScheduleSlots.slots[item.entry.startSlot]
                        val endSpec = ScheduleSlots.slots[
                            (item.entry.startSlot + item.entry.span - 1).coerceIn(0, ScheduleSlots.slots.lastIndex)
                        ]
                        val durationMin = endSpec.endMinutesOfDay - startSpec.startMinutesOfDay
                        val topY = slotTops[item.entry.startSlot] + 2.dp
                        val cardHeight = (WEEK_DP_PER_MIN * durationMin).dp - 4.dp
                        val leftX = WEEK_AXIS_WIDTH + colWidth * dayIdx + 2.dp

                        WeekGridCard(
                            item = item,
                            onClick = { onTapCard(item) },
                            modifier = Modifier
                                .offset(x = leftX, y = topY)
                                .width(colWidth - 4.dp)
                                .height(cardHeight)
                        )
                    }
                }

                // Week Holiday Banner when whole week is holiday
                if (weekHoliday != null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = RoutineColors.SurfaceCard.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, RoutineColors.AmberWarning.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(start = WEEK_AXIS_WIDTH + 12.dp, end = WEEK_END_PADDING + 12.dp, top = WEEK_TOP_PADDING + 34.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RoutineColors.AmberWarning.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EventBusy,
                                    contentDescription = null,
                                    tint = RoutineColors.AmberWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = weekHoliday.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoutineColors.TextPrimary
                                )
                                Text(
                                    text = "Holiday week - classes paused & academic weeks shifted",
                                    fontSize = 11.5.sp,
                                    color = RoutineColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekBreakRow(
    timeLabel: String,
    title: String,
    subtitle: String?,
    breakHeight: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = WEEK_END_PADDING)
            .height(breakHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(WEEK_AXIS_WIDTH)
                .fillMaxHeight()
                .padding(end = 6.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = timeLabel,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                style = RoutineType.NumericStat,
                color = WEEK_AXIS_STATIC_COLOR,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.offset(y = (-5).dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(BreakStripBrush)
                .border(0.5.dp, Color.White.copy(alpha = 0.035f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    color = RoutineColors.TextMuted
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp,
                        style = RoutineType.NumericStat,
                        color = RoutineColors.TextMuted.copy(alpha = 0.72f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekGridCard(
    item: ResolvedEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grade = remember(item.subject.colorId) { ColorPalette.byId(item.subject.colorId) }
    val isDeeper = item.entry.isLab || item.subject.isEvenCode
    val formattedCode = remember(item.displayCode) {
        val parts = item.displayCode.trim().split(" ", limit = 2)
        if (parts.size == 2) "${parts[0]}\n${parts[1]}" else item.displayCode
    }
    val cardShape = RoundedCornerShape(11.dp)
    val cardAlpha by animateFloatAsState(
        targetValue = if (item.isCancelled) 0.45f else 1f,
        animationSpec = tween(220),
        label = "weekCardAlpha"
    )

    Box(
        modifier = modifier
            .springTouchClickable(onClick = onClick)
            .graphicsLayer { alpha = cardAlpha }
            .clip(cardShape)
            .background(grade.gradientBrush(isLabTone = isDeeper))
            .background(RoutineColors.CardSheenOverlay)
            .border(
                width = if (item.isLiveNow) 1.5.dp else 1.dp,
                brush = if (item.isLiveNow) {
                    Brush.verticalGradient(listOf(RoutineColors.CyanAccent, RoutineColors.CyanAccent.copy(alpha = 0.55f)))
                } else {
                    RoutineColors.SpecularCardBorder
                },
                shape = cardShape
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = formattedCode,
                    style = RoutineType.GridCourseCode,
                    maxLines = 2
                )

                if (item.entry.teacher.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(0.7.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = item.entry.teacher,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                if (item.entry.isLab) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.Black.copy(alpha = 0.34f))
                            .border(0.7.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = "LAB",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }
            }

            if (item.isCancelled) {
                Text(
                    text = "CANC",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}

private fun formatSlotAxisAmPm(hour24: Int, minute: Int): String {
    val suffix = if (hour24 >= 12) "pm" else "am"
    val h12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return if (minute == 0) "$h12 $suffix" else "$h12:${minute.toString().padStart(2, '0')} $suffix"
}

@Composable
private fun ClassActionBottomSheet(
    resolved: ResolvedEntry,
    dbState: AppDatabaseState,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onToggleCancel: () -> Unit,
    onShiftNextWeek: () -> Unit,
    onSwap: (direction: Int) -> Unit,
    onDelete: () -> Unit
) {
    val grade = ColorPalette.byId(resolved.subject.colorId)
    val canShift = remember(resolved, dbState.entries) {
        RoutineScheduleEngine.canShiftLabNextWeek(dbState.entries, resolved.entry)
    }
    val canSwapUp = remember(resolved, dbState.entries) {
        RoutineScheduleEngine.findSwapTarget(dbState.entries, resolved.entry, resolved.academicWeek, -1) != null
    }
    val canSwapDown = remember(resolved, dbState.entries) {
        RoutineScheduleEngine.findSwapTarget(dbState.entries, resolved.entry, resolved.academicWeek, +1) != null
    }

    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Surface(
        shape = sheetShape,
        color = RoutineColors.SurfaceDark,
        border = BorderStroke(1.dp, SheetTopBorderBrush),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.24f))
                    .clickable(onClick = onDismiss)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Elevated Header Summary Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = RoutineColors.SurfaceCard,
                border = BorderStroke(1.dp, RoutineColors.SpecularGlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (resolved.entry.isLab) grade.labStart else grade.accent)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        val headerText = buildString {
                            append(resolved.displayCode)
                            if (resolved.entry.teacher.isNotBlank()) append("  \u2022  ${resolved.entry.teacher}")
                            if (resolved.entry.isLab) append("  \u2022  LAB")
                        }
                        Text(
                            text = headerText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            style = RoutineType.NumericStat,
                            color = RoutineColors.TextPrimary
                        )
                        Text(
                            text = "${resolved.date.fullDayName}, ${resolved.date.shortMonthName} ${resolved.date.day}  \u2022  ${
                                ScheduleSlots.compactRangeLabel(resolved.entry.startSlot, resolved.entry.span)
                            }",
                            fontSize = 13.sp,
                            style = RoutineType.NumericStat,
                            color = RoutineColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            SheetActionItem(
                icon = Icons.Rounded.Edit,
                label = "Edit Class",
                tint = RoutineColors.TextPrimary,
                enabled = true,
                onClick = onEdit
            )

            SheetActionItem(
                icon = if (resolved.isCancelled) Icons.Rounded.Restore else Icons.Rounded.Cancel,
                label = if (resolved.isCancelled) "Restore Class (Today)" else "Cancel Class (Today)",
                tint = if (resolved.isCancelled) RoutineColors.EmeraldSuccess else RoutineColors.AmberWarning,
                enabled = true,
                onClick = onToggleCancel
            )

            if (resolved.entry.isLab && resolved.entry.repeatPattern != RepeatPattern.EVERY_WEEK) {
                SheetActionItem(
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    label = "Shift Next Week",
                    subtitle = if (!canShift) "Opposite week occupied" else null,
                    tint = RoutineColors.CyanAccent,
                    enabled = canShift,
                    onClick = onShiftNextWeek
                )
            } else if (!resolved.entry.isLab) {
                SheetActionItem(
                    icon = Icons.Rounded.ArrowUpward,
                    label = "Swap Up with Previous Class",
                    tint = RoutineColors.TextPrimary,
                    enabled = canSwapUp,
                    onClick = { onSwap(-1) }
                )
                SheetActionItem(
                    icon = Icons.Rounded.ArrowDownward,
                    label = "Swap Down with Next Class",
                    tint = RoutineColors.TextPrimary,
                    enabled = canSwapDown,
                    onClick = { onSwap(+1) }
                )
            }

            SheetActionItem(
                icon = Icons.Rounded.DeleteOutline,
                label = "Delete from Routine",
                tint = RoutineColors.DangerRed,
                enabled = true,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun SheetActionItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    enabled: Boolean,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = RoutineColors.SurfaceCard.copy(alpha = 0.7f),
        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.34f)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tint
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = RoutineColors.TextSecondary
                )
            }
        }
    }
}
