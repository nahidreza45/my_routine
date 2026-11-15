package com.nahidreza.routine.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object RoutineColors {
    val Black = Color(0xFF050608)
    val SurfaceDark = Color(0xFF0C0F16)
    val SurfaceCard = Color(0xFF121620)
    val SurfaceElevated = Color(0xFF1A202E)
    val BorderSubtle = Color(0x1CFFFFFF)
    val BorderStrong = Color(0x36FFFFFF)
    val PrimaryPill = Color(0xFFB8D2FF)
    val PrimaryPillText = Color(0xFF070E1C)
    val CyanAccent = Color(0xFF22D3EE)
    val AmberWarning = Color(0xFFF59E0B)
    val EmeraldSuccess = Color(0xFF10B981)
    val DangerRed = Color(0xFFEF4444)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    val SpecularCardBorder: Brush = Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.06f))
    )
    val SpecularGlassBorder: Brush = Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.04f))
    )
    val CardSheenOverlay: Brush = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
        start = Offset.Zero,
        end = Offset(420f, 320f)
    )
    val DrawerAvatarRing: Brush = Brush.linearGradient(
        colors = listOf(CyanAccent, PrimaryPill, Color(0xFF6366F1))
    )
}

object RoutineType {
    val TopBarMonth = TextStyle(
        fontSize = 23.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        color = RoutineColors.TextPrimary
    )
    val ScreenTitle = TextStyle(
        fontSize = 21.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
        color = RoutineColors.TextPrimary
    )
    val SectionEyebrow = TextStyle(
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.25.sp,
        color = RoutineColors.TextMuted
    )
    val DayHeroCode = TextStyle(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
        fontFeatureSettings = "tnum",
        color = Color.White
    )
    val DayTimePill = TextStyle(
        fontSize = 12.5.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = "tnum",
        color = Color.White
    )
    val GridCourseCode = TextStyle(
        fontSize = 12.5.sp,
        lineHeight = 14.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp,
        fontFeatureSettings = "tnum",
        color = Color.White
    )
    val NumericStat = TextStyle(
        fontFeatureSettings = "tnum"
    )
}

private val RoutineDarkColorScheme = darkColorScheme(
    primary = RoutineColors.PrimaryPill,
    onPrimary = RoutineColors.PrimaryPillText,
    secondary = RoutineColors.CyanAccent,
    onSecondary = RoutineColors.Black,
    background = RoutineColors.Black,
    onBackground = RoutineColors.TextPrimary,
    surface = RoutineColors.SurfaceDark,
    onSurface = RoutineColors.TextPrimary,
    surfaceVariant = RoutineColors.SurfaceCard,
    onSurfaceVariant = RoutineColors.TextSecondary,
    outline = RoutineColors.BorderSubtle
)

private val RoutineTypography = Typography(
    titleLarge = RoutineType.TopBarMonth,
    titleMedium = RoutineType.ScreenTitle,
    labelSmall = RoutineType.SectionEyebrow
)

@Composable
fun RoutineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RoutineDarkColorScheme,
        typography = RoutineTypography,
        content = content
    )
}

/**
 * Adds crisp GPU-accelerated press scale + subtle haptic feedback without legacy `composed {}` overhead.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.springTouchClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.968f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "springTouchScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        )
}
