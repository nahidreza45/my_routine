package com.nahidreza.routine.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.nahidreza.routine.data.ResolvedEntry
import com.nahidreza.routine.model.ColorPalette
import com.nahidreza.routine.model.HolidayRange
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.ScheduleSlots
import java.io.File
import java.io.FileOutputStream

object RoutineImageExporter {

    private val BG_CANVAS = Color.parseColor("#000000")
    private val SURFACE_CARD = Color.parseColor("#0F131D")
    private val SURFACE_BREAK = Color.parseColor("#131823")
    private val BORDER_SUBTLE = Color.parseColor("#232B3A")
    private val TEXT_MUTED = Color.parseColor("#7C8AA0")
    private val TEXT_SECONDARY = Color.parseColor("#9AA6BA")
    private val CYAN_ACCENT = Color.parseColor("#38BDF8")
    private val CYAN_PILL_BG = Color.parseColor("#0D2634")
    private val AMBER_WARNING = Color.parseColor("#F59E0B")

    fun renderDayBitmap(
        date: RoutineDate,
        items: List<ResolvedEntry>,
        holiday: HolidayRange? = null
    ): Bitmap {
        val width = 1620
        val sidePad = 84f
        val topPad = 92f
        val headerHeight = 250f
        val cardHeight = 276f
        val cardGap = 34f
        val bottomPad = 88f

        val bodyHeight = when {
            holiday != null -> 460f
            items.isEmpty() -> 420f
            else -> items.size * cardHeight + (items.size - 1).coerceAtLeast(0) * cardGap
        }
        val totalHeight = (topPad + headerHeight + bodyHeight + bottomPad).toInt().coerceAtLeast(1080)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BG_CANVAS)

        val boldType = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val normalType = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Header: Day on top, "dd Month YYYY" below (single month, no week badge)
        val dayTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 78f
            typeface = boldType
        }
        val dateSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 38f
            typeface = normalType
        }

        val dayTitleY = topPad + 72f
        val dateSubY = dayTitleY + 58f
        canvas.drawText(date.fullDayName, sidePad, dayTitleY, dayTitlePaint)
        canvas.drawText("${date.day} ${date.monthName} ${date.year}", sidePad, dateSubY, dateSubPaint)

        drawBrandPill(canvas, rightEdge = width - sidePad, centerY = topPad + 70f)

        val dividerY = topPad + headerHeight - 48f
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BORDER_SUBTLE
            strokeWidth = 2f
        }
        canvas.drawLine(sidePad, dividerY, width - sidePad, dividerY, dividerPaint)

        val contentStartY = topPad + headerHeight
        when {
            holiday != null -> {
                drawHolidayBanner(
                    canvas = canvas,
                    left = sidePad,
                    top = contentStartY + 24f,
                    right = width - sidePad,
                    title = holiday.name,
                    subtitle = "Classes are paused for this holiday."
                )
            }

            items.isEmpty() -> {
                val emptyBox = RectF(sidePad, contentStartY + 12f, width - sidePad, contentStartY + 340f)
                val boxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = SURFACE_CARD }
                val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = BORDER_SUBTLE
                }
                canvas.drawRoundRect(emptyBox, 48f, 48f, boxBg)
                canvas.drawRoundRect(emptyBox, 48f, 48f, boxStroke)

                val emptyTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = TEXT_SECONDARY
                    textSize = 42f
                    typeface = boldType
                    textAlign = Paint.Align.CENTER
                }
                val emptySub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = TEXT_MUTED
                    textSize = 30f
                    typeface = normalType
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("No Classes Scheduled", emptyBox.centerX(), emptyBox.centerY() - 10f, emptyTitle)
                canvas.drawText("Enjoy your free day", emptyBox.centerX(), emptyBox.centerY() + 44f, emptySub)
            }

            else -> {
                var y = contentStartY
                for (item in items) {
                    val grade = ColorPalette.byId(item.subject.colorId)
                    val isDeeper = item.entry.isLab || item.subject.isEvenCode
                    val startColor = if (isDeeper) grade.labStartArgb.toInt() else grade.theoryStartArgb.toInt()
                    val endColor = if (isDeeper) grade.labEndArgb.toInt() else grade.theoryEndArgb.toInt()

                    val rect = RectF(sidePad, y, width - sidePad, y + cardHeight)
                    val corner = 54f

                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = LinearGradient(
                            rect.left, rect.top, rect.right, rect.bottom,
                            startColor, endColor, Shader.TileMode.CLAMP
                        )
                        alpha = if (item.isCancelled) 122 else 255
                    }
                    canvas.drawRoundRect(rect, corner, corner, bgPaint)

                    val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = LinearGradient(
                            rect.left, rect.top, rect.left, rect.bottom,
                            Color.argb(if (item.isCancelled) 12 else 28, 255, 255, 255),
                            Color.argb(18, 0, 0, 0),
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawRoundRect(rect, corner, corner, sheenPaint)

                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 2.5f
                        shader = LinearGradient(
                            rect.left, rect.top, rect.left, rect.bottom,
                            Color.argb(66, 255, 255, 255),
                            Color.argb(20, 255, 255, 255),
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawRoundRect(rect, corner, corner, borderPaint)

                    val innerPadX = 52f
                    val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = if (item.isCancelled) Color.argb(190, 255, 255, 255) else Color.WHITE
                        textSize = 60f
                        typeface = boldType
                    }
                    val codeY = rect.top + 104f
                    canvas.drawText(item.displayCode, rect.left + innerPadX, codeY, codePaint)

                    var badgeX = rect.left + innerPadX + codePaint.measureText(item.displayCode) + 28f
                    val badgeTop = rect.top + 48f
                    if (item.entry.teacher.isNotBlank()) {
                        badgeX += drawDayChip(canvas, item.entry.teacher, badgeX, badgeTop, darker = false) + 18f
                    }
                    if (item.entry.isLab) {
                        drawDayChip(canvas, "LAB", badgeX, badgeTop, darker = true)
                    }

                    if (item.isCancelled) {
                        drawDayCancelledTag(canvas, rightEdge = rect.right - 44f, top = rect.top + 44f)
                    }

                    val timeLabel = ScheduleSlots.timeRangeLabel(item.entry.startSlot, item.entry.span)
                    drawTimePill(
                        canvas = canvas,
                        text = timeLabel,
                        left = rect.left + innerPadX,
                        bottom = rect.bottom - 44f,
                        dimmed = item.isCancelled
                    )

                    y += cardHeight + cardGap
                }
            }
        }

        return bitmap
    }

    fun renderWeekBitmap(
        weekDates: List<RoutineDate>,
        academicWeek: Int,
        byDay: List<List<ResolvedEntry>>,
        weekHoliday: HolidayRange? = null
    ): Bitmap {
        val width = 2160
        val outerSidePad = 56f
        val axisWidth = 178f
        val gridLeft = outerSidePad + axisWidth
        val gridRight = width - outerSidePad
        val gridWidth = gridRight - gridLeft
        val colW = gridWidth / 5f

        val topPad = 88f
        val headerBlockH = 304f // Week badge + range header + SAT..WED day strip
        val gridTop = topPad + headerBlockH

        val pxPerMin = 5.85f
        val slotH = 50f * pxPerMin
        val teaBreakH = 20f * pxPerMin
        val lunchBreakH = 70f * pxPerMin
        val gridBottomPadding = 38f
        val bottomMargin = 72f

        val gridHeight = 540f * pxPerMin
        val totalHeight = (gridTop + gridHeight + gridBottomPadding + bottomMargin).toInt()

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BG_CANVAS)

        val boldType = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val weekAxisColor = Color.parseColor("#CBD5E1")
        val weekGridLineColor = Color.argb(40, 255, 255, 255)

        // Header: "WEEK X" pill + non-redundant date range on left, ROUTINE badge on right
        val weekBadgeRight = drawWeekBadge(
            canvas = canvas,
            label = "WEEK $academicWeek",
            left = outerSidePad,
            top = topPad
        )

        val rangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 56f
            typeface = boldType
        }
        val rangeLabel = formatWeekRangeLabel(weekDates)
        canvas.drawText(rangeLabel, weekBadgeRight + 28f, topPad + 56f, rangePaint)

        drawBrandPill(canvas, rightEdge = width - outerSidePad, centerY = topPad + 38f)

        val dividerY = topPad + 118f
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BORDER_SUBTLE
            strokeWidth = 2.5f
        }
        canvas.drawLine(outerSidePad, dividerY, width - outerSidePad, dividerY, dividerPaint)

        // SAT..WED Column Header Strip (high-contrast white/slate)
        val dayNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = weekAxisColor
            textSize = 36f
            letterSpacing = 0.08f
            typeface = boldType
            textAlign = Paint.Align.CENTER
        }
        val dayNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            typeface = boldType
            textAlign = Paint.Align.CENTER
        }
        weekDates.forEachIndexed { idx, d ->
            val cx = gridLeft + idx * colW + colW / 2f
            canvas.drawText(d.shortDayName, cx, topPad + 190f, dayNamePaint)
            canvas.drawText(d.day.toString(), cx, topPad + 256f, dayNumPaint)
        }

        // Compute slot tops & break band Y positions first (before drawing lines)
        val slotTops = FloatArray(9)
        var teaBreakTop = 0f
        var lunchBreakTop = 0f
        var y = gridTop
        for (i in 0..8) {
            if (i == 3) {
                teaBreakTop = y
                y += teaBreakH
            } else if (i == 6) {
                lunchBreakTop = y
                y += lunchBreakH
            }
            slotTops[i] = y
            y += slotH
        }
        val gridBottom = y
        val teaBreakBottom = teaBreakTop + teaBreakH
        val lunchBreakBottom = lunchBreakTop + lunchBreakH

        // Draw grid lines strictly in the 3 class sections (zero vertical lines across break bands)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = weekGridLineColor
            strokeWidth = 2.2f
        }
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = weekAxisColor
            textSize = 33f
            typeface = boldType
            textAlign = Paint.Align.RIGHT
        }

        val segmentRanges = listOf(
            gridTop to teaBreakTop,
            teaBreakBottom to lunchBreakTop,
            lunchBreakBottom to gridBottom
        )
        for ((segTop, segBottom) in segmentRanges) {
            for (c in 0..5) {
                val x = gridLeft + c * colW
                canvas.drawLine(x, segTop, x, segBottom, linePaint)
            }
        }
        for (i in 0..8) {
            val topY = slotTops[i]
            val bottomY = topY + slotH
            canvas.drawLine(gridLeft, topY, gridRight, topY, linePaint)
            canvas.drawLine(gridLeft, bottomY, gridRight, bottomY, linePaint)

            val slotSpec = ScheduleSlots.slots[i]
            canvas.drawText(
                formatAxisAmPm(slotSpec.startHour24, slotSpec.startMinute),
                gridLeft - 20f,
                topY + 12f,
                axisPaint
            )
        }
        canvas.drawText("5 pm", gridLeft - 20f, gridBottom + 12f, axisPaint)

        // Draw clean opaque break bands with top/bottom borders and high-contrast labels
        drawWeekBreakRow(
            canvas = canvas,
            axisRight = gridLeft - 20f,
            gridLeft = gridLeft,
            gridRight = gridRight,
            y = teaBreakTop,
            height = teaBreakH,
            timeLabel = "10:30 am",
            title = "TEA BREAK",
            subtitle = null
        )
        drawWeekBreakRow(
            canvas = canvas,
            axisRight = gridLeft - 20f,
            gridLeft = gridLeft,
            gridRight = gridRight,
            y = lunchBreakTop,
            height = lunchBreakH,
            timeLabel = "1:20 pm",
            title = "LUNCH & PRAYER BREAK",
            subtitle = "1:20 - 2:30 PM"
        )

        // Draw cards with larger high-contrast typography sized cleanly for each grid cell
        byDay.forEachIndexed { dayIdx, list ->
            for (item in list) {
                val grade = ColorPalette.byId(item.subject.colorId)
                val isDeeper = item.entry.isLab || item.subject.isEvenCode
                val sColor = if (isDeeper) grade.labStartArgb.toInt() else grade.theoryStartArgb.toInt()
                val eColor = if (isDeeper) grade.labEndArgb.toInt() else grade.theoryEndArgb.toInt()

                val startSpec = ScheduleSlots.slots[item.entry.startSlot]
                val endSpec = ScheduleSlots.slots[
                    (item.entry.startSlot + item.entry.span - 1).coerceIn(0, ScheduleSlots.slots.lastIndex)
                ]
                val durationMin = endSpec.endMinutesOfDay - startSpec.startMinutesOfDay

                val left = gridLeft + dayIdx * colW + 8f
                val right = gridLeft + (dayIdx + 1) * colW - 8f
                val top = slotTops[item.entry.startSlot] + 6f
                val bottom = top + durationMin * pxPerMin - 12f
                val rect = RectF(left, top, right, bottom)
                val corner = 30f

                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        sColor, eColor, Shader.TileMode.CLAMP
                    )
                    alpha = if (item.isCancelled) 122 else 255
                }
                canvas.drawRoundRect(rect, corner, corner, bgPaint)

                val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.left, rect.bottom,
                        Color.argb(28, 255, 255, 255),
                        Color.argb(18, 0, 0, 0),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(rect, corner, corner, sheenPaint)

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2.4f
                    shader = LinearGradient(
                        rect.left, rect.top, rect.left, rect.bottom,
                        Color.argb(74, 255, 255, 255),
                        Color.argb(24, 255, 255, 255),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(rect, corner, corner, borderPaint)

                val parts = item.displayCode.trim().split(" ", limit = 2)
                val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (item.isCancelled) Color.argb(210, 255, 255, 255) else Color.WHITE
                    textSize = 54f
                    typeface = boldType
                }
                var ty = rect.top + 60f
                for (p in parts) {
                    canvas.drawText(p, rect.left + 22f, ty, codePaint)
                    ty += 56f
                }

                ty += 12f
                if (item.entry.teacher.isNotBlank() && ty + 48f < rect.bottom - 10f) {
                    drawWeekChip(canvas, item.entry.teacher, rect.left + 20f, ty, darker = false)
                    ty += 58f
                }
                if (item.entry.isLab && ty + 48f < rect.bottom - 10f) {
                    drawWeekChip(canvas, "LAB", rect.left + 20f, ty, darker = true)
                }

                if (item.isCancelled) {
                    val cancPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 27f
                        letterSpacing = 0.08f
                        typeface = boldType
                    }
                    canvas.drawText("CANC", rect.left + 22f, rect.bottom - 18f, cancPaint)
                }
            }
        }

        if (weekHoliday != null) {
            drawHolidayBanner(
                canvas = canvas,
                left = gridLeft + 28f,
                top = gridTop + 64f,
                right = gridRight - 28f,
                title = weekHoliday.name,
                subtitle = "Holiday week - classes paused & academic weeks shifted"
            )
        }

        return bitmap
    }

    private fun drawBrandPill(canvas: Canvas, rightEdge: Float, centerY: Float) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 26f
            letterSpacing = 0.14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val label = "ROUTINE"
        val pillW = textPaint.measureText(label) + 56f
        val pillH = 62f
        val rect = RectF(rightEdge - pillW, centerY - pillH / 2f, rightEdge, centerY + pillH / 2f)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = SURFACE_CARD }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = BORDER_SUBTLE
        }
        canvas.drawRoundRect(rect, 31f, 31f, bg)
        canvas.drawRoundRect(rect, 31f, 31f, stroke)
        canvas.drawText(label, rect.left + 28f, rect.centerY() + 9f, textPaint)
    }

    private fun drawWeekBadge(canvas: Canvas, label: String, left: Float, top: Float): Float {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = CYAN_ACCENT
            textSize = 32f
            letterSpacing = 0.08f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val pillW = textPaint.measureText(label) + 56f
        val pillH = 72f
        val rect = RectF(left, top, left + pillW, top + pillH)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CYAN_PILL_BG }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.argb(120, 56, 189, 248)
        }
        canvas.drawRoundRect(rect, 36f, 36f, bg)
        canvas.drawRoundRect(rect, 36f, 36f, stroke)
        canvas.drawText(label, rect.left + 28f, rect.centerY() + 11f, textPaint)
        return rect.right
    }

    private fun drawHolidayBanner(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        title: String,
        subtitle: String
    ) {
        val rect = RectF(left, top, right, top + 240f)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(242, 15, 19, 29)
        }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.argb(120, 245, 158, 11)
        }
        canvas.drawRoundRect(rect, 40f, 40f, bg)
        canvas.drawRoundRect(rect, 40f, 40f, stroke)

        val circleCx = rect.left + 96f
        val circleCy = rect.centerY()
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(38, 245, 158, 11)
        }
        canvas.drawCircle(circleCx, circleCy, 44f, circlePaint)

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AMBER_WARNING
            textSize = 26f
            letterSpacing = 0.1f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("OFF", circleCx, circleCy + 9f, badgePaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val textX = circleCx + 76f
        canvas.drawText(title, textX, rect.centerY() - 10f, titlePaint)
        canvas.drawText(subtitle, textX, rect.centerY() + 38f, subPaint)
    }

    private fun drawDayChip(canvas: Canvas, text: String, x: Float, y: Float, darker: Boolean): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val w = paint.measureText(text) + 48f
        val rect = RectF(x, y, x + w, y + 66f)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darker) Color.argb(86, 0, 0, 0) else Color.argb(46, 255, 255, 255)
        }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            color = Color.argb(58, 255, 255, 255)
        }
        canvas.drawRoundRect(rect, 20f, 20f, bg)
        canvas.drawRoundRect(rect, 20f, 20f, stroke)
        canvas.drawText(text, rect.left + 24f, rect.centerY() + 12f, paint)
        return w
    }

    private fun drawDayCancelledTag(canvas: Canvas, rightEdge: Float, top: Float) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            letterSpacing = 0.06f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val label = "CANCELLED"
        val w = textPaint.measureText(label) + 48f
        val rect = RectF(rightEdge - w, top, rightEdge, top + 62f)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(115, 0, 0, 0) }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            color = Color.argb(55, 255, 255, 255)
        }
        canvas.drawRoundRect(rect, 31f, 31f, bg)
        canvas.drawRoundRect(rect, 31f, 31f, stroke)
        canvas.drawText(label, rect.left + 24f, rect.centerY() + 10f, textPaint)
    }

    private fun drawTimePill(
        canvas: Canvas,
        text: String,
        left: Float,
        bottom: Float,
        dimmed: Boolean
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (dimmed) Color.argb(190, 255, 255, 255) else Color.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val iconRadius = 15f
        val iconGap = 18f
        val leftPad = 30f
        val rightPad = 32f
        val w = leftPad + iconRadius * 2 + iconGap + textPaint.measureText(text) + rightPad
        val h = 76f
        val rect = RectF(left, bottom - h, left + w, bottom)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(if (dimmed) 28 else 46, 255, 255, 255)
        }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            color = Color.argb(54, 255, 255, 255)
        }
        canvas.drawRoundRect(rect, h / 2f, h / 2f, bg)
        canvas.drawRoundRect(rect, h / 2f, h / 2f, stroke)

        val clockCx = rect.left + leftPad + iconRadius
        val clockCy = rect.centerY()
        val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.8f
            strokeCap = Paint.Cap.ROUND
            color = if (dimmed) Color.argb(190, 255, 255, 255) else Color.WHITE
        }
        canvas.drawCircle(clockCx, clockCy, iconRadius, clockPaint)
        canvas.drawLine(clockCx, clockCy - 7f, clockCx, clockCy + 1f, clockPaint)
        canvas.drawLine(clockCx, clockCy + 1f, clockCx + 6f, clockCy + 4f, clockPaint)

        canvas.drawText(
            text,
            clockCx + iconRadius + iconGap,
            rect.centerY() + 12f,
            textPaint
        )
    }

    private fun drawWeekBreakRow(
        canvas: Canvas,
        axisRight: Float,
        gridLeft: Float,
        gridRight: Float,
        y: Float,
        height: Float,
        timeLabel: String,
        title: String,
        subtitle: String?
    ) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = SURFACE_BREAK }
        canvas.drawRect(gridLeft, y, gridRight, y + height, bg)

        val bandBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(46, 255, 255, 255)
            strokeWidth = 2f
        }
        canvas.drawLine(gridLeft, y, gridRight, y, bandBorder)
        canvas.drawLine(gridLeft, y + height, gridRight, y + height, bandBorder)

        val boldType = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 33f
            typeface = boldType
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(timeLabel, axisRight, y + 12f, tPaint)

        val cx = (gridLeft + gridRight) / 2f
        val lblPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 33f
            letterSpacing = 0.16f
            typeface = boldType
            textAlign = Paint.Align.CENTER
        }
        if (subtitle.isNullOrBlank()) {
            canvas.drawText(title, cx, y + height / 2f + 11f, lblPaint)
        } else {
            canvas.drawText(title, cx, y + height / 2f - 14f, lblPaint)
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CBD5E1")
                textSize = 29f
                letterSpacing = 0.06f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(subtitle, cx, y + height / 2f + 30f, subPaint)
        }
    }

    private fun drawWeekChip(canvas: Canvas, text: String, x: Float, y: Float, darker: Boolean) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 33f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val w = paint.measureText(text) + 38f
        val rect = RectF(x, y, x + w, y + 48f)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darker) Color.argb(104, 0, 0, 0) else Color.argb(56, 255, 255, 255)
        }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.argb(82, 255, 255, 255)
        }
        canvas.drawRoundRect(rect, 14f, 14f, bg)
        canvas.drawRoundRect(rect, 14f, 14f, stroke)
        canvas.drawText(text, rect.left + 19f, rect.centerY() + 11f, paint)
    }

    private fun formatWeekRangeLabel(weekDates: List<RoutineDate>): String {
        val first = weekDates.firstOrNull() ?: return "Weekly Routine"
        val last = weekDates.lastOrNull() ?: first
        return when {
            first.year == last.year && first.month == last.month -> {
                "${first.day} - ${last.day} ${first.monthName} ${first.year}"
            }
            first.year == last.year -> {
                "${first.day} ${first.shortMonthName} - ${last.day} ${last.shortMonthName} ${first.year}"
            }
            else -> {
                "${first.day} ${first.shortMonthName} ${first.year} - ${last.day} ${last.shortMonthName} ${last.year}"
            }
        }
    }

    private fun formatAxisAmPm(hour24: Int, minute: Int): String {
        val suffix = if (hour24 >= 12) "pm" else "am"
        val h12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return if (minute == 0) "$h12 $suffix" else "$h12:${minute.toString().padStart(2, '0')} $suffix"
    }

    fun saveToGallery(context: Context, bitmap: Bitmap): Boolean {
        val filename = "ROUTINE_${System.currentTimeMillis()}.png"
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ROUTINE")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return false
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
                true
            } else {
                @Suppress("DEPRECATION")
                val picDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "ROUTINE"
                )
                picDir.mkdirs()
                val file = File(picDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
                true
            }
        }.getOrDefault(false)
    }

    fun createShareIntent(context: Context, bitmap: Bitmap): Intent? {
        return runCatching {
            val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
            val file = File(dir, "routine_share.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Intent.createChooser(sendIntent, "Share Routine")
        }.getOrNull()
    }
}
