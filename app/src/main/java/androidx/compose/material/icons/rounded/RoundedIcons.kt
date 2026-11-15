package androidx.compose.material.icons.rounded

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.buildLayeredRoutineIcon
import androidx.compose.material.icons.buildRoutineIcon
import androidx.compose.material.icons.circle
import androidx.compose.material.icons.filledPath
import androidx.compose.material.icons.roundedRect
import androidx.compose.material.icons.strokedPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector

private var _add: ImageVector? = null
val Icons.Rounded.Add: ImageVector
    get() {
        if (_add != null) return _add!!
        _add = buildRoutineIcon(name = "Rounded.Add") {
            moveTo(18f, 13f)
            horizontalLineToRelative(-5f)
            verticalLineToRelative(5f)
            curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
            reflectiveCurveToRelative(-1f, -0.45f, -1f, -1f)
            verticalLineToRelative(-5f)
            horizontalLineTo(6f)
            curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f)
            reflectiveCurveToRelative(0.45f, -1f, 1f, -1f)
            horizontalLineToRelative(5f)
            verticalLineTo(6f)
            curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f)
            reflectiveCurveToRelative(1f, 0.45f, 1f, 1f)
            verticalLineToRelative(5f)
            horizontalLineToRelative(5f)
            curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
            reflectiveCurveToRelative(-0.45f, 1f, -1f, 1f)
            close()
        }
        return _add!!
    }

private var _arrowDownward: ImageVector? = null
val Icons.Rounded.ArrowDownward: ImageVector
    get() {
        if (_arrowDownward != null) return _arrowDownward!!
        _arrowDownward = buildRoutineIcon(name = "Rounded.ArrowDownward") {
            moveTo(19.29f, 11.29f)
            lineTo(13f, 17.59f)
            verticalLineTo(5f)
            curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
            reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
            verticalLineToRelative(12.59f)
            lineToRelative(-6.29f, -6.3f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
            curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
            lineToRelative(8f, 8f)
            curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
            lineToRelative(8f, -8f)
            curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
            curveToRelative(-0.39f, -0.38f, -1.03f, -0.39f, -1.42f, 0f)
            close()
        }
        return _arrowDownward!!
    }

private var _arrowUpward: ImageVector? = null
val Icons.Rounded.ArrowUpward: ImageVector
    get() {
        if (_arrowUpward != null) return _arrowUpward!!
        _arrowUpward = buildRoutineIcon(name = "Rounded.ArrowUpward") {
            moveTo(4.71f, 12.71f)
            lineTo(11f, 6.41f)
            verticalLineTo(19f)
            curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
            reflectiveCurveToRelative(1f, -0.45f, 1f, -1f)
            verticalLineTo(6.41f)
            lineToRelative(6.29f, 6.3f)
            curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
            curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
            lineToRelative(-8f, -8f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
            lineToRelative(-8f, 8f)
            curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
            curveToRelative(0.39f, 0.38f, 1.03f, 0.39f, 1.42f, 0f)
            close()
        }
        return _arrowUpward!!
    }

private var _book: ImageVector? = null
val Icons.Rounded.Book: ImageVector
    get() {
        if (_book != null) return _book!!
        _book = buildRoutineIcon(
            name = "Rounded.Book",
            pathFillType = PathFillType.EvenOdd,
        ) {
            roundedRect(4f, 2f, 20f, 22f, 2f)
            moveTo(7f, 4f)
            horizontalLineTo(11.5f)
            verticalLineTo(11.5f)
            lineTo(9.25f, 10f)
            lineTo(7f, 11.5f)
            close()
        }
        return _book!!
    }

private var _calendarMonth: ImageVector? = null
val Icons.Rounded.CalendarMonth: ImageVector
    get() {
        if (_calendarMonth != null) return _calendarMonth!!
        _calendarMonth = buildRoutineIcon(
            name = "Rounded.CalendarMonth",
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(19f, 4f)
            horizontalLineTo(18f)
            verticalLineTo(2.5f)
            curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
            reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
            verticalLineTo(4f)
            horizontalLineTo(8f)
            verticalLineTo(2.5f)
            curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
            reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
            verticalLineTo(4f)
            horizontalLineTo(5f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineTo(19.5f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineTo(19f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(6f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            roundedRect(5f, 9f, 19f, 19.5f, 0.8f)
            roundedRect(7f, 11.2f, 9f, 13.2f, 0.5f)
            roundedRect(11f, 11.2f, 13f, 13.2f, 0.5f)
            roundedRect(15f, 11.2f, 17f, 13.2f, 0.5f)
            roundedRect(7f, 15.2f, 9f, 17.2f, 0.5f)
            roundedRect(11f, 15.2f, 13f, 17.2f, 0.5f)
            roundedRect(15f, 15.2f, 17f, 17.2f, 0.5f)
        }
        return _calendarMonth!!
    }

private var _calendarToday: ImageVector? = null
val Icons.Rounded.CalendarToday: ImageVector
    get() {
        if (_calendarToday != null) return _calendarToday!!
        _calendarToday = buildRoutineIcon(
            name = "Rounded.CalendarToday",
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(20f, 3f)
            horizontalLineTo(19f)
            verticalLineTo(2f)
            curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
            reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
            verticalLineTo(3f)
            horizontalLineTo(7f)
            verticalLineTo(2f)
            curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
            reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
            verticalLineTo(3f)
            horizontalLineTo(4f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineTo(19f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineTo(20f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(5f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            roundedRect(4f, 9.5f, 20f, 19f, 0.8f)
        }
        return _calendarToday!!
    }

private var _cancel: ImageVector? = null
val Icons.Rounded.Cancel: ImageVector
    get() {
        if (_cancel != null) return _cancel!!
        _cancel = buildRoutineIcon(
            name = "Rounded.Cancel",
            pathFillType = PathFillType.EvenOdd,
        ) {
            circle(12f, 12f, 10f)
            moveTo(16.3f, 16.3f)
            curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0f)
            lineTo(12f, 13.41f)
            lineTo(9.11f, 16.3f)
            curveToRelative(-0.39f, 0.39f, -1.02f, 0.39f, -1.41f, 0f)
            curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0f, -1.41f)
            lineTo(10.59f, 12f)
            lineTo(7.7f, 9.11f)
            curveToRelative(-0.39f, -0.39f, -0.39f, -1.02f, 0f, -1.41f)
            curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0f)
            lineTo(12f, 10.59f)
            lineToRelative(2.89f, -2.89f)
            curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0f)
            curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0f, 1.41f)
            lineTo(13.41f, 12f)
            lineToRelative(2.89f, 2.89f)
            curveToRelative(0.38f, 0.38f, 0.38f, 1.02f, 0f, 1.41f)
            close()
        }
        return _cancel!!
    }

private var _check: ImageVector? = null
val Icons.Rounded.Check: ImageVector
    get() {
        if (_check != null) return _check!!
        _check = buildRoutineIcon(name = "Rounded.Check") {
            moveTo(9f, 16.2f)
            lineTo(5.5f, 12.7f)
            curveToRelative(-0.39f, -0.39f, -1.01f, -0.39f, -1.4f, 0f)
            curveToRelative(-0.39f, 0.39f, -0.39f, 1.01f, 0f, 1.4f)
            lineToRelative(4.19f, 4.19f)
            curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
            lineTo(20.3f, 7.7f)
            curveToRelative(0.39f, -0.39f, 0.39f, -1.01f, 0f, -1.4f)
            curveToRelative(-0.39f, -0.39f, -1.01f, -0.39f, -1.4f, 0f)
            lineTo(9f, 16.2f)
            close()
        }
        return _check!!
    }

private var _checkCircleOutline: ImageVector? = null
val Icons.Rounded.CheckCircleOutline: ImageVector
    get() {
        if (_checkCircleOutline != null) return _checkCircleOutline!!
        _checkCircleOutline = buildLayeredRoutineIcon(name = "Rounded.CheckCircleOutline") {
            filledPath(pathFillType = PathFillType.EvenOdd) {
                circle(12f, 12f, 10f)
                circle(12f, 12f, 8f)
            }
            strokedPath(strokeWidth = 2f) {
                moveTo(8f, 12.2f)
                lineTo(10.8f, 15f)
                lineTo(16.2f, 9.2f)
            }
        }
        return _checkCircleOutline!!
    }

private var _close: ImageVector? = null
val Icons.Rounded.Close: ImageVector
    get() {
        if (_close != null) return _close!!
        _close = buildRoutineIcon(name = "Rounded.Close") {
            moveTo(18.3f, 5.71f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
            lineTo(12f, 10.59f)
            lineTo(7.11f, 5.7f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
            curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
            lineTo(10.59f, 12f)
            lineTo(5.7f, 16.89f)
            curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
            curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
            lineTo(12f, 13.41f)
            lineToRelative(4.89f, 4.89f)
            curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
            curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
            lineTo(13.41f, 12f)
            lineToRelative(4.89f, -4.89f)
            curveToRelative(0.38f, -0.38f, 0.38f, -1.02f, 0f, -1.4f)
            close()
        }
        return _close!!
    }

private var _deleteOutline: ImageVector? = null
val Icons.Rounded.DeleteOutline: ImageVector
    get() {
        if (_deleteOutline != null) return _deleteOutline!!
        _deleteOutline = buildRoutineIcon(
            name = "Rounded.DeleteOutline",
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(15.5f, 4f)
            lineToRelative(-0.71f, -0.71f)
            curveToRelative(-0.18f, -0.18f, -0.44f, -0.29f, -0.7f, -0.29f)
            horizontalLineTo(9.91f)
            curveToRelative(-0.26f, 0f, -0.52f, 0.11f, -0.7f, 0.29f)
            lineTo(8.5f, 4f)
            horizontalLineTo(6f)
            curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
            reflectiveCurveToRelative(0.45f, 1f, 1f, 1f)
            horizontalLineToRelative(12f)
            curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
            reflectiveCurveToRelative(-0.45f, -1f, -1f, -1f)
            horizontalLineToRelative(-2.5f)
            close()
            roundedRect(6f, 7f, 18f, 21f, 2f)
            roundedRect(8f, 9f, 16f, 19f, 1f)
        }
        return _deleteOutline!!
    }

private var _download: ImageVector? = null
val Icons.Rounded.Download: ImageVector
    get() {
        if (_download != null) return _download!!
        _download = buildRoutineIcon(name = "Rounded.Download") {
            roundedRect(5f, 18f, 19f, 20f, 1f)
            moveTo(16.59f, 9f)
            horizontalLineTo(15f)
            verticalLineTo(4f)
            curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
            horizontalLineToRelative(-4f)
            curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
            verticalLineToRelative(5f)
            horizontalLineTo(7.41f)
            curveToRelative(-0.89f, 0f, -1.34f, 1.08f, -0.71f, 1.71f)
            lineToRelative(4.59f, 4.59f)
            curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
            lineToRelative(4.59f, -4.59f)
            curveToRelative(0.63f, -0.63f, 0.19f, -1.71f, -0.7f, -1.71f)
            close()
        }
        return _download!!
    }

private var _edit: ImageVector? = null
val Icons.Rounded.Edit: ImageVector
    get() {
        if (_edit != null) return _edit!!
        _edit = buildRoutineIcon(name = "Rounded.Edit") {
            moveTo(3f, 17.46f)
            verticalLineToRelative(3.04f)
            curveToRelative(0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
            horizontalLineToRelative(3.04f)
            curveToRelative(0.13f, 0f, 0.26f, -0.05f, 0.35f, -0.15f)
            lineTo(17.81f, 9.94f)
            lineToRelative(-3.75f, -3.75f)
            lineTo(3.15f, 17.1f)
            curveToRelative(-0.1f, 0.1f, -0.15f, 0.22f, -0.15f, 0.36f)
            close()
            moveTo(20.71f, 7.04f)
            curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
            lineToRelative(-2.34f, -2.34f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
            lineToRelative(-1.83f, 1.83f)
            lineToRelative(3.75f, 3.75f)
            lineToRelative(1.83f, -1.83f)
            close()
        }
        return _edit!!
    }

private var _eventBusy: ImageVector? = null
val Icons.Rounded.EventBusy: ImageVector
    get() {
        if (_eventBusy != null) return _eventBusy!!
        _eventBusy = buildLayeredRoutineIcon(name = "Rounded.EventBusy") {
            filledPath(pathFillType = PathFillType.EvenOdd) {
                moveTo(19f, 4f)
                horizontalLineTo(18f)
                verticalLineTo(2.5f)
                curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
                reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
                verticalLineTo(4f)
                horizontalLineTo(8f)
                verticalLineTo(2.5f)
                curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
                reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
                verticalLineTo(4f)
                horizontalLineTo(5f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineTo(19.5f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineTo(19f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(6f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                roundedRect(5f, 9f, 19f, 19.5f, 0.8f)
            }
            strokedPath(strokeWidth = 2f) {
                moveTo(9.5f, 11.7f)
                lineTo(14.5f, 16.7f)
                moveTo(14.5f, 11.7f)
                lineTo(9.5f, 16.7f)
            }
        }
        return _eventBusy!!
    }

private var _fastForward: ImageVector? = null
val Icons.Rounded.FastForward: ImageVector
    get() {
        if (_fastForward != null) return _fastForward!!
        _fastForward = buildRoutineIcon(name = "Rounded.FastForward") {
            moveTo(5.58f, 16.89f)
            lineToRelative(5.77f, -4.07f)
            curveToRelative(0.56f, -0.4f, 0.56f, -1.24f, 0f, -1.63f)
            lineTo(5.58f, 7.11f)
            curveTo(4.91f, 6.65f, 4f, 7.12f, 4f, 7.93f)
            verticalLineToRelative(8.14f)
            curveToRelative(0f, 0.81f, 0.91f, 1.28f, 1.58f, 0.82f)
            close()
            moveTo(13f, 7.93f)
            verticalLineToRelative(8.14f)
            curveToRelative(0f, 0.81f, 0.91f, 1.28f, 1.58f, 0.82f)
            lineToRelative(5.77f, -4.07f)
            curveToRelative(0.56f, -0.4f, 0.56f, -1.24f, 0f, -1.63f)
            lineToRelative(-5.77f, -4.07f)
            curveTo(13.91f, 6.65f, 13f, 7.12f, 13f, 7.93f)
            close()
        }
        return _fastForward!!
    }

private var _groups: ImageVector? = null
val Icons.Rounded.Groups: ImageVector
    get() {
        if (_groups != null) return _groups!!
        _groups = buildRoutineIcon(name = "Rounded.Groups") {
            // Center person
            circle(12f, 8f, 2.5f)
            moveTo(7.5f, 18.5f)
            curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
            horizontalLineToRelative(7f)
            curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
            verticalLineTo(15.6f)
            curveToRelative(0f, -2.0f, -2.0f, -3.1f, -4.5f, -3.1f)
            reflectiveCurveToRelative(-4.5f, 1.1f, -4.5f, 3.1f)
            verticalLineTo(18.5f)
            close()
            // Left person
            circle(5f, 10.5f, 1.8f)
            moveTo(2f, 17.5f)
            curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
            horizontalLineToRelative(3.4f)
            verticalLineTo(14.1f)
            curveToRelative(-0.4f, -0.1f, -0.9f, -0.1f, -1.4f, -0.1f)
            curveToRelative(-1.8f, 0f, -3f, 0.9f, -3f, 2.4f)
            verticalLineTo(17.5f)
            close()
            // Right person
            circle(19f, 10.5f, 1.8f)
            moveTo(17.6f, 14.1f)
            verticalLineTo(18.5f)
            horizontalLineTo(21f)
            curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
            verticalLineTo(16.4f)
            curveToRelative(0f, -1.5f, -1.2f, -2.4f, -3f, -2.4f)
            curveToRelative(-0.5f, 0f, -1.0f, 0f, -1.4f, 0.1f)
            close()
        }
        return _groups!!
    }

private var _localOffer: ImageVector? = null
val Icons.Rounded.LocalOffer: ImageVector
    get() {
        if (_localOffer != null) return _localOffer!!
        _localOffer = buildRoutineIcon(
            name = "Rounded.LocalOffer",
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(21.41f, 11.58f)
            lineToRelative(-9f, -9f)
            curveTo(12.05f, 2.22f, 11.55f, 2f, 11f, 2f)
            horizontalLineTo(4f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(7f)
            curveToRelative(0f, 0.55f, 0.22f, 1.05f, 0.59f, 1.42f)
            lineToRelative(9f, 9f)
            curveToRelative(0.36f, 0.36f, 0.86f, 0.58f, 1.41f, 0.58f)
            curveToRelative(0.55f, 0f, 1.05f, -0.22f, 1.41f, -0.59f)
            lineToRelative(7f, -7f)
            curveToRelative(0.37f, -0.36f, 0.59f, -0.86f, 0.59f, -1.41f)
            curveToRelative(0f, -0.55f, -0.23f, -1.06f, -0.59f, -1.42f)
            close()
            circle(5.5f, 5.5f, 1.5f)
        }
        return _localOffer!!
    }

private var _menu: ImageVector? = null
val Icons.Rounded.Menu: ImageVector
    get() {
        if (_menu != null) return _menu!!
        _menu = buildRoutineIcon(name = "Rounded.Menu") {
            roundedRect(3f, 6f, 21f, 8f, 1f)
            roundedRect(3f, 11f, 21f, 13f, 1f)
            roundedRect(3f, 16f, 21f, 18f, 1f)
        }
        return _menu!!
    }

private var _menuBook: ImageVector? = null
val Icons.Rounded.MenuBook: ImageVector
    get() {
        if (_menuBook != null) return _menuBook!!
        _menuBook = buildLayeredRoutineIcon(name = "Rounded.MenuBook") {
            strokedPath(strokeWidth = 1.9f) {
                // Left page
                moveTo(12f, 6.5f)
                curveTo(10.2f, 5.2f, 7.2f, 4.8f, 3.2f, 5.2f)
                lineTo(3.2f, 18.2f)
                curveTo(7.2f, 17.8f, 10.2f, 18.4f, 12f, 19.7f)
                // Right page
                moveTo(12f, 6.5f)
                curveTo(13.8f, 5.2f, 16.8f, 4.8f, 20.8f, 5.2f)
                lineTo(20.8f, 18.2f)
                curveTo(16.8f, 17.8f, 13.8f, 18.4f, 12f, 19.7f)
                // Center spine
                moveTo(12f, 6.5f)
                verticalLineTo(19.7f)
            }
            strokedPath(strokeWidth = 1.5f) {
                moveTo(15f, 9.6f)
                lineTo(18.2f, 9.6f)
                moveTo(15f, 12.8f)
                lineTo(18.2f, 12.8f)
            }
        }
        return _menuBook!!
    }

private var _moreVert: ImageVector? = null
val Icons.Rounded.MoreVert: ImageVector
    get() {
        if (_moreVert != null) return _moreVert!!
        _moreVert = buildRoutineIcon(name = "Rounded.MoreVert") {
            circle(12f, 6f, 2f)
            circle(12f, 12f, 2f)
            circle(12f, 18f, 2f)
        }
        return _moreVert!!
    }

private var _notificationsActive: ImageVector? = null
val Icons.Rounded.NotificationsActive: ImageVector
    get() {
        if (_notificationsActive != null) return _notificationsActive!!
        _notificationsActive = buildLayeredRoutineIcon(name = "Rounded.NotificationsActive") {
            filledPath {
                // Clapper
                moveTo(12f, 22f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                horizontalLineToRelative(-4f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                close()
                // Bell body
                moveTo(18f, 16f)
                verticalLineTo(11f)
                curveToRelative(0f, -2.9f, -1.6f, -5.3f, -4.5f, -6f)
                verticalLineTo(4.2f)
                curveToRelative(0f, -0.8f, -0.7f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveToRelative(-1.5f, 0.7f, -1.5f, 1.5f)
                verticalLineTo(5f)
                curveTo(7.6f, 5.7f, 6f, 8.1f, 6f, 11f)
                verticalLineTo(16f)
                lineTo(4.7f, 17.3f)
                curveToRelative(-0.6f, 0.6f, -0.2f, 1.7f, 0.7f, 1.7f)
                horizontalLineToRelative(13.2f)
                curveToRelative(0.9f, 0f, 1.3f, -1.1f, 0.7f, -1.7f)
                lineTo(18f, 16f)
                close()
            }
            strokedPath(strokeWidth = 1.8f) {
                moveTo(4.2f, 6.4f)
                curveTo(3.1f, 7.8f, 2.5f, 9.6f, 2.5f, 11.5f)
                moveTo(19.8f, 6.4f)
                curveTo(20.9f, 7.8f, 21.5f, 9.6f, 21.5f, 11.5f)
            }
        }
        return _notificationsActive!!
    }

private var _remove: ImageVector? = null
val Icons.Rounded.Remove: ImageVector
    get() {
        if (_remove != null) return _remove!!
        _remove = buildRoutineIcon(name = "Rounded.Remove") {
            roundedRect(5f, 11f, 19f, 13f, 1f)
        }
        return _remove!!
    }

private var _repeat: ImageVector? = null
val Icons.Rounded.Repeat: ImageVector
    get() {
        if (_repeat != null) return _repeat!!
        _repeat = buildRoutineIcon(name = "Rounded.Repeat") {
            moveTo(7f, 7f)
            horizontalLineToRelative(10f)
            verticalLineToRelative(1.79f)
            curveToRelative(0f, 0.45f, 0.54f, 0.67f, 0.85f, 0.35f)
            lineToRelative(2.79f, -2.79f)
            curveToRelative(0.2f, -0.2f, 0.2f, -0.51f, 0f, -0.71f)
            lineToRelative(-2.79f, -2.79f)
            curveTo(17.54f, 2.54f, 17f, 2.76f, 17f, 3.21f)
            verticalLineTo(5f)
            horizontalLineTo(6f)
            curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
            verticalLineToRelative(4f)
            curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
            reflectiveCurveToRelative(1f, -0.45f, 1f, -1f)
            verticalLineTo(7f)
            close()
            moveTo(17f, 17f)
            horizontalLineTo(7f)
            verticalLineToRelative(-1.79f)
            curveToRelative(0f, -0.45f, -0.54f, -0.67f, -0.85f, -0.35f)
            lineToRelative(-2.79f, 2.79f)
            curveToRelative(-0.2f, 0.2f, -0.2f, 0.51f, 0f, 0.71f)
            lineToRelative(2.79f, 2.79f)
            curveToRelative(0.31f, 0.31f, 0.85f, 0.09f, 0.85f, -0.36f)
            verticalLineTo(19f)
            horizontalLineToRelative(11f)
            curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
            verticalLineToRelative(-4f)
            curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
            reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
            verticalLineToRelative(3f)
            close()
        }
        return _repeat!!
    }

private var _restore: ImageVector? = null
val Icons.Rounded.Restore: ImageVector
    get() {
        if (_restore != null) return _restore!!
        _restore = buildLayeredRoutineIcon(name = "Rounded.Restore") {
            strokedPath(strokeWidth = 2f) {
                moveTo(7.4f, 17.5f)
                arcTo(
                    horizontalEllipseRadius = 7.8f,
                    verticalEllipseRadius = 7.8f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 5.2f,
                    y1 = 12f,
                )
                // Arrowhead at (5.2, 12) pointing down
                moveTo(2.8f, 10.2f)
                lineTo(5.2f, 12.6f)
                lineTo(7.6f, 10.2f)
                // Clock hands inside
                moveTo(13f, 8.2f)
                lineTo(13f, 12.2f)
                lineTo(16f, 14f)
            }
        }
        return _restore!!
    }

private var _save: ImageVector? = null
val Icons.Rounded.Save: ImageVector
    get() {
        if (_save != null) return _save!!
        _save = buildRoutineIcon(
            name = "Rounded.Save",
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(17.59f, 3.59f)
            curveToRelative(-0.38f, -0.38f, -0.89f, -0.59f, -1.42f, -0.59f)
            horizontalLineTo(5f)
            curveToRelative(-1.11f, 0f, -2f, 0.9f, -2f, 2f)
            verticalLineToRelative(14f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(14f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(7.83f)
            curveToRelative(0f, -0.53f, -0.21f, -1.04f, -0.59f, -1.41f)
            lineToRelative(-2.82f, -2.83f)
            close()
            circle(12f, 15f, 3f)
            roundedRect(6f, 5f, 15f, 9f, 1f)
        }
        return _save!!
    }

private var _schedule: ImageVector? = null
val Icons.Rounded.Schedule: ImageVector
    get() {
        if (_schedule != null) return _schedule!!
        _schedule = buildLayeredRoutineIcon(name = "Rounded.Schedule") {
            filledPath(pathFillType = PathFillType.EvenOdd) {
                circle(12f, 12f, 10f)
                circle(12f, 12f, 8f)
            }
            strokedPath(strokeWidth = 2f) {
                moveTo(12f, 7.5f)
                lineTo(12f, 12.2f)
                lineTo(15.6f, 14.3f)
            }
        }
        return _schedule!!
    }

private var _settings: ImageVector? = null
val Icons.Rounded.Settings: ImageVector
    get() {
        if (_settings != null) return _settings!!
        _settings = buildRoutineIcon(
            name = "Rounded.Settings",
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(19.42f, 10.34f)
            lineTo(21.99f, 10.49f)
            lineTo(21.99f, 13.51f)
            lineTo(19.42f, 13.66f)
            lineTo(17.14f, 17.59f)
            lineTo(18.30f, 19.89f)
            lineTo(15.69f, 21.40f)
            lineTo(14.27f, 19.25f)
            lineTo(9.73f, 19.25f)
            lineTo(8.31f, 21.40f)
            lineTo(5.70f, 19.89f)
            lineTo(6.86f, 17.59f)
            lineTo(4.58f, 13.66f)
            lineTo(2.01f, 13.51f)
            lineTo(2.01f, 10.49f)
            lineTo(4.58f, 10.34f)
            lineTo(6.86f, 6.41f)
            lineTo(5.70f, 4.11f)
            lineTo(8.31f, 2.60f)
            lineTo(9.73f, 4.75f)
            lineTo(14.27f, 4.75f)
            lineTo(15.69f, 2.60f)
            lineTo(18.30f, 4.11f)
            lineTo(17.14f, 6.41f)
            close()
            circle(12f, 12f, 3.6f)
        }
        return _settings!!
    }

private var _share: ImageVector? = null
val Icons.Rounded.Share: ImageVector
    get() {
        if (_share != null) return _share!!
        _share = buildLayeredRoutineIcon(name = "Rounded.Share") {
            strokedPath(strokeWidth = 2.1f) {
                moveTo(17.5f, 5.5f)
                lineTo(6.5f, 12f)
                lineTo(17.5f, 18.5f)
            }
            filledPath {
                circle(18f, 5.5f, 3f)
                circle(6f, 12f, 3f)
                circle(18f, 18.5f, 3f)
            }
        }
        return _share!!
    }

private var _upload: ImageVector? = null
val Icons.Rounded.Upload: ImageVector
    get() {
        if (_upload != null) return _upload!!
        _upload = buildRoutineIcon(name = "Rounded.Upload") {
            roundedRect(5f, 18f, 19f, 20f, 1f)
            moveTo(7.41f, 10f)
            horizontalLineTo(9f)
            verticalLineToRelative(5f)
            curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
            horizontalLineToRelative(4f)
            curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
            verticalLineToRelative(-5f)
            horizontalLineToRelative(1.59f)
            curveToRelative(0.89f, 0f, 1.34f, -1.08f, 0.71f, -1.71f)
            lineTo(12.71f, 3.7f)
            curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
            lineTo(6.71f, 8.29f)
            curveToRelative(-0.63f, 0.63f, -0.19f, 1.71f, 0.7f, 1.71f)
            close()
        }
        return _upload!!
    }

private var _viewDay: ImageVector? = null
val Icons.Rounded.ViewDay: ImageVector
    get() {
        if (_viewDay != null) return _viewDay!!
        _viewDay = buildRoutineIcon(name = "Rounded.ViewDay") {
            roundedRect(3f, 4f, 21f, 6.5f, 1f)
            roundedRect(3f, 8.5f, 21f, 15.5f, 1.5f)
            roundedRect(3f, 17.5f, 21f, 20f, 1f)
        }
        return _viewDay!!
    }

private var _viewWeek: ImageVector? = null
val Icons.Rounded.ViewWeek: ImageVector
    get() {
        if (_viewWeek != null) return _viewWeek!!
        _viewWeek = buildRoutineIcon(
            name = "Rounded.ViewWeek",
            pathFillType = PathFillType.EvenOdd,
        ) {
            roundedRect(3f, 5f, 21f, 19f, 2f)
            roundedRect(4.8f, 7f, 8.6f, 17f, 0.8f)
            roundedRect(10.1f, 7f, 13.9f, 17f, 0.8f)
            roundedRect(15.4f, 7f, 19.2f, 17f, 0.8f)
        }
        return _viewWeek!!
    }
