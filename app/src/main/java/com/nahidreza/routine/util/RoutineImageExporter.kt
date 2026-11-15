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
import com.nahidreza.routine.model.RoutineDate
import com.nahidreza.routine.model.ScheduleSlots
import java.io.File
import java.io.FileOutputStream

object RoutineImageExporter {

    fun renderDayBitmap(date: RoutineDate, items: List<ResolvedEntry>): Bitmap {
        val width = 1080
        val padding = 64f
        val headerHeight = 200f
        val cardHeight = 210f
        val cardGap = 28f
        val footerHeight = 90f
        val bodyHeight = if (items.isEmpty()) 300f else items.size * (cardHeight + cardGap)
        val totalHeight = (headerHeight + bodyHeight + footerHeight).toInt().coerceAtLeast(640)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        canvas.drawText("${date.fullDayName}, ${date.shortMonthName} ${date.day}", padding, 96f, titlePaint)
        canvas.drawText("${date.monthName} ${date.year}  \u2022  ROUTINE", padding, 148f, subPaint)

        if (items.isEmpty()) {
            val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 36f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("No classes scheduled", width / 2f, headerHeight + 130f, emptyPaint)
        } else {
            var y = headerHeight
            for (item in items) {
                val grade = ColorPalette.byId(item.subject.colorId)
                val isDeeper = item.entry.isLab || item.subject.isEvenCode
                val startColor = if (isDeeper) grade.labStartArgb.toInt() else grade.theoryStartArgb.toInt()
                val endColor = if (isDeeper) grade.labEndArgb.toInt() else grade.theoryEndArgb.toInt()

                val rect = RectF(padding, y, width - padding, y + cardHeight)
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        startColor, endColor, Shader.TileMode.CLAMP
                    )
                    alpha = if (item.isCancelled) 135 else 255
                }
                canvas.drawRoundRect(rect, 38f, 38f, bgPaint)

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                    color = Color.argb(45, 255, 255, 255)
                }
                canvas.drawRoundRect(rect, 38f, 38f, borderPaint)

                val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 46f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(item.displayCode, rect.left + 40f, rect.top + 82f, codePaint)

                var badgeX = rect.left + 40f + codePaint.measureText(item.displayCode) + 22f
                if (item.entry.teacher.isNotBlank()) {
                    badgeX += drawBadge(canvas, item.entry.teacher, badgeX, rect.top + 40f, false) + 14f
                }
                if (item.entry.isLab) {
                    drawBadge(canvas, "LAB", badgeX, rect.top + 40f, true)
                }

                if (item.isCancelled) {
                    val cancelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(75, 0, 0, 0)
                    }
                    val tagRect = RectF(rect.right - 220f, rect.top, rect.right, rect.top + 54f)
                    canvas.drawRoundRect(tagRect, 20f, 20f, cancelPaint)
                    val tagText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 24f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("CANCELLED", tagRect.centerX(), tagRect.centerY() + 9f, tagText)
                }

                val timeLabel = ScheduleSlots.timeRangeLabel(item.entry.startSlot, item.entry.span)
                val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 28f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val pillW = pillTextPaint.measureText(timeLabel) + 52f
                val pillRect = RectF(rect.left + 40f, rect.bottom - 86f, rect.left + 40f + pillW, rect.bottom - 30f)
                val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(42, 255, 255, 255)
                }
                canvas.drawRoundRect(pillRect, 28f, 28f, pillBg)
                canvas.drawText(timeLabel, pillRect.left + 26f, pillRect.centerY() + 10f, pillTextPaint)

                y += cardHeight + cardGap
            }
        }

        return bitmap
    }

    fun renderWeekBitmap(
        weekDates: List<RoutineDate>,
        byDay: List<List<ResolvedEntry>>
    ): Bitmap {
        val width = 1440
        val axisW = 140f
        val rightPad = 32f
        val headerH = 190f
        val pxPerMin = 3.1f
        val slotH = 50f * pxPerMin
        val teaBreakH = 20f * pxPerMin
        val lunchBreakH = 70f * pxPerMin
        val totalH = (headerH + 540f * pxPerMin + 60f).toInt()

        val bitmap = Bitmap.createBitmap(width, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)

        val monthText = weekDates.firstOrNull()?.let { "${it.monthName} ${it.year}" } ?: "Routine"
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("$monthText  \u2022  Weekly Routine", 42f, 76f, headerPaint)

        val colW = (width - axisW - rightPad) / 5f
        val dayNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val dayNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        weekDates.forEachIndexed { idx, d ->
            val cx = axisW + idx * colW + colW / 2f
            canvas.drawText(d.shortDayName, cx, 126f, dayNamePaint)
            canvas.drawText(d.day.toString(), cx, 168f, dayNumPaint)
        }

        val slotTops = FloatArray(9)
        var y = headerH
        for (i in 0..8) {
            if (i == 3) {
                drawBreakRow(canvas, axisW, rightPad, width.toFloat(), y, teaBreakH, "10:30", "TEA BREAK")
                y += teaBreakH
            } else if (i == 6) {
                drawBreakRow(canvas, axisW, rightPad, width.toFloat(), y, lunchBreakH, "1:20", "LUNCH & PRAYER BREAK")
                y += lunchBreakH
            }
            slotTops[i] = y
            y += slotH
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(24, 255, 255, 255)
            strokeWidth = 2f
        }
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 24f
            textAlign = Paint.Align.RIGHT
        }

        for (i in 0..8) {
            val topY = slotTops[i]
            canvas.drawLine(axisW, topY, width - rightPad, topY, linePaint)
            canvas.drawText(ScheduleSlots.slots[i].axisLabel, axisW - 18f, topY + 12f, axisPaint)
        }
        for (c in 0..5) {
            val x = axisW + c * colW
            canvas.drawLine(x, headerH, x, y, linePaint)
        }

        // Draw cards
        byDay.forEachIndexed { dayIdx, list ->
            for (item in list) {
                val grade = ColorPalette.byId(item.subject.colorId)
                val isDeeper = item.entry.isLab || item.subject.isEvenCode
                val sColor = if (isDeeper) grade.labStartArgb.toInt() else grade.theoryStartArgb.toInt()
                val eColor = if (isDeeper) grade.labEndArgb.toInt() else grade.theoryEndArgb.toInt()

                val left = axisW + dayIdx * colW + 6f
                val right = axisW + (dayIdx + 1) * colW - 6f
                val top = slotTops[item.entry.startSlot] + 4f
                val bottom = top + item.entry.span * slotH - 10f
                val rect = RectF(left, top, right, bottom)

                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        sColor, eColor, Shader.TileMode.CLAMP
                    )
                    alpha = if (item.isCancelled) 130 else 255
                }
                canvas.drawRoundRect(rect, 20f, 20f, bgPaint)

                val parts = item.displayCode.split(" ")
                val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 28f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                var ty = rect.top + 38f
                for (p in parts.take(2)) {
                    canvas.drawText(p, rect.left + 16f, ty, codePaint)
                    ty += 32f
                }

                if (item.entry.teacher.isNotBlank()) {
                    drawSmallChip(canvas, item.entry.teacher, rect.left + 14f, ty + 4f, false)
                    ty += 42f
                }
                if (item.entry.isLab) {
                    drawSmallChip(canvas, "LAB", rect.left + 14f, ty + 4f, true)
                }
            }
        }

        return bitmap
    }

    private fun drawBreakRow(
        canvas: Canvas,
        axisW: Float,
        rightPad: Float,
        width: Float,
        y: Float,
        h: Float,
        timeLabel: String,
        title: String
    ) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#181B22") }
        canvas.drawRect(axisW, y, width - rightPad, y + h, bg)

        val tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 22f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(timeLabel, axisW - 18f, y + h / 2f + 8f, tPaint)

        val lblPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 22f
            letterSpacing = 0.14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, (axisW + width - rightPad) / 2f, y + h / 2f + 8f, lblPaint)
    }

    private fun drawBadge(canvas: Canvas, text: String, x: Float, y: Float, darker: Boolean): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val w = paint.measureText(text) + 32f
        val rect = RectF(x, y, x + w, y + 48f)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darker) Color.argb(72, 0, 0, 0) else Color.argb(44, 255, 255, 255)
        }
        canvas.drawRoundRect(rect, 14f, 14f, bg)
        canvas.drawText(text, rect.left + 16f, rect.centerY() + 9f, paint)
        return w
    }

    private fun drawSmallChip(canvas: Canvas, text: String, x: Float, y: Float, darker: Boolean) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val w = paint.measureText(text) + 24f
        val rect = RectF(x, y, x + w, y + 34f)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darker) Color.argb(72, 0, 0, 0) else Color.argb(42, 255, 255, 255)
        }
        canvas.drawRoundRect(rect, 10f, 10f, bg)
        canvas.drawText(text, rect.left + 12f, rect.centerY() + 7f, paint)
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
