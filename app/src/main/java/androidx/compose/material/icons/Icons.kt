package androidx.compose.material.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object Icons {
    object Rounded

    object AutoMirrored {
        object Rounded
    }
}

internal inline fun buildRoutineIcon(
    name: String,
    autoMirror: Boolean = false,
    pathFillType: PathFillType = PathFillType.NonZero,
    block: PathBuilder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    autoMirror = autoMirror,
).apply {
    filledPath(pathFillType = pathFillType, block = block)
}.build()

internal inline fun buildLayeredRoutineIcon(
    name: String,
    autoMirror: Boolean = false,
    block: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    autoMirror = autoMirror,
).apply(block).build()

internal inline fun ImageVector.Builder.filledPath(
    pathFillType: PathFillType = PathFillType.NonZero,
    block: PathBuilder.() -> Unit,
) {
    path(
        fill = SolidColor(Color.Black),
        fillAlpha = 1f,
        stroke = null,
        strokeAlpha = 1f,
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = pathFillType,
        pathBuilder = block,
    )
}

internal inline fun ImageVector.Builder.strokedPath(
    strokeWidth: Float = 2f,
    strokeCap: StrokeCap = StrokeCap.Round,
    strokeJoin: StrokeJoin = StrokeJoin.Round,
    block: PathBuilder.() -> Unit,
) {
    path(
        fill = null,
        fillAlpha = 1f,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1f,
        strokeLineWidth = strokeWidth,
        strokeLineCap = strokeCap,
        strokeLineJoin = strokeJoin,
        strokeLineMiter = 4f,
        pathFillType = PathFillType.NonZero,
        pathBuilder = block,
    )
}

internal fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    val k = r * 0.55228475f
    moveTo(cx, cy - r)
    curveTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy)
    curveTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r)
    curveTo(cx - k, cy + r, cx - r, cy + k, cx - r, cy)
    curveTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r)
    close()
}

internal fun PathBuilder.roundedRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float,
) {
    val r = radius.coerceAtMost(minOf(right - left, bottom - top) / 2f)
    moveTo(left + r, top)
    horizontalLineTo(right - r)
    curveTo(right - r * 0.45f, top, right, top + r * 0.45f, right, top + r)
    verticalLineTo(bottom - r)
    curveTo(right, bottom - r * 0.45f, right - r * 0.45f, bottom, right - r, bottom)
    horizontalLineTo(left + r)
    curveTo(left + r * 0.45f, bottom, left, bottom - r * 0.45f, left, bottom - r)
    verticalLineTo(top + r)
    curveTo(left, top + r * 0.45f, left + r * 0.45f, top, left + r, top)
    close()
}
