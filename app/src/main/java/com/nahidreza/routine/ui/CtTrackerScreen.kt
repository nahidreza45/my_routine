package com.nahidreza.routine.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.ColorPalette
import com.nahidreza.routine.model.CtCourseRecord
import com.nahidreza.routine.model.Subject

@Composable
fun CtTrackerScreen(
    dbState: AppDatabaseState,
    onUpdateMark: (subjectId: String, index: Int, score: Int?) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val theoryCourses = remember(dbState.subjects) {
        dbState.subjects.filter { !it.isEvenCode }
    }
    val recordMap = remember(dbState.ctRecords) {
        dbState.ctRecords.associateBy { it.subjectId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoutineColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Header
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
                Column {
                    Text(
                        text = "CT Marks",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoutineColors.TextPrimary
                    )
                    Text(
                        text = "Best 3 of 4 \u2022 Out of 20",
                        fontSize = 12.sp,
                        color = RoutineColors.TextMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = RoutineColors.SurfaceCard,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Text(
                    text = "${theoryCourses.size} Courses",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RoutineColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                )
            }
        }

        if (theoryCourses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add odd-numbered theory subjects in Manage Subjects to track CT marks.",
                    color = RoutineColors.TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(theoryCourses, key = { it.id }) { subject ->
                    val record = recordMap[subject.id] ?: CtCourseRecord(subjectId = subject.id)
                    CtCourseCard(
                        subject = subject,
                        record = record,
                        onUpdateMark = { index, value -> onUpdateMark(subject.id, index, value) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CtCourseCard(
    subject: Subject,
    record: CtCourseRecord,
    onUpdateMark: (Int, Int?) -> Unit
) {
    val grade = ColorPalette.byId(subject.colorId)
    val bestIndices = record.bestThreeIndices()
    val finalCeil = record.bestThreeAverageCeil()
    val animatedFinal by animateIntAsState(
        targetValue = finalCeil,
        animationSpec = tween(220),
        label = "ctAvgAnim"
    )
    val marks = record.activeMarks()
    val hasScores = marks.any { it != null }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = RoutineColors.SurfaceCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(50))
                            .background(grade.accent)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = subject.code,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            style = RoutineType.NumericStat,
                            color = RoutineColors.TextPrimary
                        )
                        if (subject.teachers.isNotEmpty()) {
                            Text(
                                text = subject.teachers.joinToString(", "),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = RoutineColors.TextMuted
                            )
                        }
                    }
                }

                // Minimal Top-3 Average Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(grade.gradientBrush(isLabTone = false))
                        .background(RoutineColors.CardSheenOverlay)
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(13.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "AVG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color.White.copy(alpha = 0.78f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Text(
                            text = if (hasScores) animatedFinal.toString() else "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = RoutineType.NumericStat,
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                marks.forEachIndexed { idx, value ->
                    val isBest = idx in bestIndices
                    CtScoreInputBox(
                        label = "CT ${idx + 1}",
                        score = value,
                        isBestThree = isBest,
                        accentColor = grade.accent,
                        onScoreChange = { newVal -> onUpdateMark(idx, newVal) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CtScoreInputBox(
    label: String,
    score: Int?,
    isBestThree: Boolean,
    accentColor: Color,
    onScoreChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(score) { mutableStateOf(score?.toString().orEmpty()) }
    LaunchedEffect(score) {
        val currentInt = textValue.toIntOrNull()
        if (currentInt != score) {
            textValue = score?.toString().orEmpty()
        }
    }

    val boxBg by animateColorAsState(
        targetValue = if (isBestThree) accentColor.copy(alpha = 0.12f) else Color(0xFF0E1118),
        animationSpec = tween(200),
        label = "ctBoxBg"
    )
    val boxBorder by animateColorAsState(
        targetValue = if (isBestThree) accentColor.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(200),
        label = "ctBoxBorder"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isBestThree) Color.White.copy(alpha = 0.88f) else RoutineColors.TextMuted,
        animationSpec = tween(200),
        label = "ctBoxLabel"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
            color = labelColor
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(boxBg)
                .border(
                    width = if (isBestThree) 1.4.dp else 1.dp,
                    color = boxBorder,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() }.take(2)
                    val parsed = filtered.toIntOrNull()
                    if (parsed != null && parsed > 20) {
                        textValue = "20"
                        onScoreChange(20)
                    } else {
                        textValue = filtered
                        onScoreChange(parsed)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(accentColor),
                textStyle = TextStyle(
                    color = if (isBestThree) Color.White else Color.White.copy(alpha = 0.45f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                    textAlign = TextAlign.Center
                ),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (textValue.isEmpty()) {
                            Text(
                                text = "-",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.25f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
