package uz.electro.remote.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.electro.remote.ui.theme.*

/**
 * Обогрев и вентиляция сидений без цифр.
 *
 * Раньше уровень 0..3 выбирался четырьмя чипами с числами — на четыре сиденья
 * это 32 кнопки с цифрами. Здесь уровень показывает сама фигура: у обогрева
 * загораются полоски снизу вверх, у вентиляции — лопасти пропеллера, и он
 * начинает крутиться тем быстрее, чем выше уровень. Нажатие переключает
 * уровень по кругу, последнее нажатие выключает.
 */

const val SEAT_LEVELS = 3

/** Следующий уровень по кругу: 0 → 1 → 2 → 3 → 0. */
fun nextSeatLevel(level: Int): Int = if (level >= SEAT_LEVELS) 0 else level + 1

/**
 * Пропеллер на четыре лопасти. Лопасти зажигаются по одной, а на максимуме
 * горит весь круг — так видно, что дальше уровня нет.
 */
@Composable
fun PropellerFan(level: Int, modifier: Modifier = Modifier, dim: Dp = 44.dp) {
    val lit = when {
        level <= 0 -> 0
        level >= SEAT_LEVELS -> BLADES
        else -> level
    }
    val transition = rememberInfiniteTransition(label = "fan")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 2600 / level.coerceIn(1, SEAT_LEVELS), easing = LinearEasing)
        ),
        label = "spin",
    )
    val angle = if (level > 0) spin else 0f
    // Canvas рисует вне композиции, палитру туда передаём значением.
    val palette = ElectroColors

    Canvas(modifier.size(dim)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        val hub = r * 0.22f
        val bladeW = r * 0.62f
        val len = r * 0.98f
        repeat(BLADES) { i ->
            rotate(angle + i * 90f, c) {
                drawOval(
                    // вентиляция — синим (обогрев рисуется оранжевым в HeatBars)
                    color = if (i < lit) palette.Info else palette.SurfaceRaised,
                    topLeft = Offset(c.x - bladeW / 2f, c.y - len),
                    size = Size(bladeW, len - hub * 0.6f),
                )
            }
        }
        drawCircle(
            if (lit > 0) palette.Info else palette.TextDisabled,
            hub, c,
        )
    }
}

private const val BLADES = 4

/** Три полоски обогрева: горят снизу вверх, выключено — не горит ни одна. */
@Composable
fun HeatBars(level: Int, modifier: Modifier = Modifier, dim: Dp = 44.dp) {
    val palette = ElectroColors
    Canvas(modifier.size(dim)) {
        val bar = size.height / 5f
        repeat(SEAT_LEVELS) { i ->
            val top = size.height - (i + 1) * bar * 2f + bar
            drawRoundRect(
                color = if (i < level) palette.Warn else palette.SurfaceRaised,
                topLeft = Offset(0f, top),
                size = Size(size.width, bar),
                cornerRadius = CornerRadius(bar / 2f),
            )
        }
    }
}

/**
 * Одно сиденье: слева обогрев, справа вентиляция. Подпись уровня не нужна —
 * его видно по фигуре.
 */
@Composable
fun SeatControl(
    name: String,
    heat: Int,
    vent: Int,
    modifier: Modifier = Modifier,
    onHeat: (Int) -> Unit,
    onVent: (Int) -> Unit,
) {
    // Цвет карточки по состоянию: обогрев → оранжевый, обдув → синий, иначе
    // нейтральный. Обогрев и обдув на одном кресле взаимоисключают друг друга.
    val bg = when {
        heat > 0 -> ElectroColors.WarnTint
        vent > 0 -> ElectroColors.InfoTint
        else -> ElectroColors.SurfaceElevated
    }
    val label = when {
        heat > 0 -> ElectroColors.Warn
        vent > 0 -> ElectroColors.Info
        else -> ElectroColors.TextSecondary
    }
    Surface(color = bg, shape = Radius.Md, modifier = modifier) {
        Column(
            Modifier.padding(vertical = Space.x3),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(name, style = ElectroType.Label, color = label)
            Spacer(Modifier.height(Space.x3))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.x2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // форму отклика задаём сами: иначе рябь рисуется квадратом
                HeatBars(
                    heat,
                    Modifier.clip(Radius.Sm).clickable { onHeat(nextSeatLevel(heat)) }.padding(Space.x2),
                )
                PropellerFan(
                    vent,
                    Modifier.clip(CircleShape).clickable { onVent(nextSeatLevel(vent)) }.padding(Space.x2),
                )
            }
        }
    }
}
