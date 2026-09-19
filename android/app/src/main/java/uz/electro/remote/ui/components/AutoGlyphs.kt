package uz.electro.remote.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Автомобильные HVAC-глифы, которых нет в Material Icons — по образцу авто-набора
 * (Car Equipment Symbols): обдув/обогрев лобового и заднего стекла, обогрев зеркал.
 * Обводочный стиль (round), тонируются через Icon(tint=…), как обычные иконки.
 * viewport 24×24.
 */
private fun autoGlyph(nm: String, body: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(nm, 24.dp, 24.dp, 24f, 24f).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = body,
        )
    }.build()

/** Три «волны тепла», поднимающиеся вверх под стеклом/зеркалом. */
private fun PathBuilder.heatWaves(xs: List<Float>, top: Float, bottom: Float) {
    for (x in xs) {
        val mid = (top + bottom) / 2f
        moveTo(x, bottom)
        curveTo(x - 1.6f, bottom - 1.3f, x + 1.6f, mid + 1.3f, x, mid)
        curveTo(x - 1.6f, mid - 1.3f, x + 1.6f, top + 1.3f, x, top)
    }
}

/** Обдув/обогрев ЛОБОВОГО — стекло-трапеция с вогнутым низом + волны тепла. */
val IconWindshieldDefrost: ImageVector by lazy {
    autoGlyph("WindshieldDefrost") {
        moveTo(3f, 4f)
        lineTo(21f, 4f)
        lineTo(18.5f, 12f)
        curveTo(15.5f, 13.6f, 8.5f, 13.6f, 5.5f, 12f)
        close()
        heatWaves(listOf(8f, 12f, 16f), 15f, 21f)
    }
}

/** Обогрев СТЁКОЛ (заднее стекло) — прямоугольник + волны тепла. */
val IconRearDefrost: ImageVector by lazy {
    autoGlyph("RearDefrost") {
        moveTo(4.5f, 3.5f)
        lineTo(19.5f, 3.5f)
        lineTo(19.5f, 12.5f)
        lineTo(4.5f, 12.5f)
        close()
        heatWaves(listOf(8f, 12f, 16f), 15f, 21f)
    }
}

/** Обогрев боковых ЗЕРКАЛ — корпус зеркала (скруг. параллелограмм) + волны. */
val IconMirrorHeat: ImageVector by lazy {
    autoGlyph("MirrorHeat") {
        moveTo(4f, 6.5f)
        curveTo(4f, 5.5f, 4.8f, 5f, 6f, 5f)
        lineTo(18f, 5f)
        curveTo(19.2f, 5f, 20f, 5.6f, 19.7f, 6.8f)
        lineTo(18.6f, 11.2f)
        curveTo(18.4f, 12.2f, 17.6f, 12.5f, 16.5f, 12.5f)
        lineTo(5.5f, 12.5f)
        curveTo(4.6f, 12.5f, 4f, 12f, 4f, 11f)
        close()
        heatWaves(listOf(8f, 12f, 16f), 15f, 21f)
    }
}
