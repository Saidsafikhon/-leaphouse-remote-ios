package uz.electro.remote.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Токены цвета Electro Remote. Один в один с коллекцией переменных `Electro`
 * в Figma-файле «Electro Remote — Design System».
 *
 * Наборов два: тёмный — исходный, снятый пипеткой с обложки, и светлый — для
 * дня. Какой действует, решает система (Настройки → Тёмная тема, в том числе
 * по расписанию «от заката до рассвета»), см. [ElectroTheme]. Экраны берут
 * цвета через `ElectroColors` и о выборе не знают.
 */
class ElectroPalette(
    // поверхности
    val Background: Color,
    val Surface: Color,
    val SurfaceElevated: Color,
    val SurfacePressed: Color,
    val SurfaceRaised: Color,
    val Outline: Color,

    // акцент. Полная заливка только у главного действия, Soft — подложка
    // активного пункта.
    val Accent: Color,
    val AccentPressed: Color,
    val AccentSoft: Color,
    val OnAccent: Color,

    // текст
    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextMuted: Color,
    val TextDisabled: Color,

    // статусы
    val Ok: Color,
    val Warn: Color,
    val Danger: Color,
    val Info: Color,

    // подложки статусных плашек
    val OkTint: Color,
    val WarnTint: Color,
    val DangerTint: Color,
    val InfoTint: Color,
)

/**
 * Ночной набор — тот, с которого рисовался макет. Поверхности и текст сняты
 * пипеткой с обложки — почти чёрные вместо синеватых. Акцент фирменный,
 * лаймовый, из UI Kit. Статусный зелёный намеренно другой, чтобы «на охране»
 * не читалось как кнопка.
 */
val ElectroDarkColors = ElectroPalette(
    // поверхности — почти чёрные, разница между уровнями едва заметна
    Background = Color(0xFF070A0D),
    Surface = Color(0xFF11161A),
    SurfaceElevated = Color(0xFF171E23),
    SurfacePressed = Color(0xFF1F282E),
    SurfaceRaised = Color(0xFF26313A),
    Outline = Color(0xFF212A30),

    Accent = Color(0xFF7CFF3B),
    AccentPressed = Color(0xFF6BE032),
    AccentSoft = Color(0xFF172C14),
    OnAccent = Color(0xFF08130A),

    TextPrimary = Color(0xFFF5F7F8),
    TextSecondary = Color(0xFFA7ADB3),
    // 4.8:1 на Surface — проверено.
    TextMuted = Color(0xFF7B838B),
    TextDisabled = Color(0xFF3A4249),

    Ok = Color(0xFF22C55E),
    Warn = Color(0xFFF0A73C),
    Danger = Color(0xFFFF5A52),
    Info = Color(0xFF4EA8F5),

    OkTint = Color(0xFF0C2418),
    WarnTint = Color(0xFF2A2010),
    DangerTint = Color(0xFF2B1414),
    InfoTint = Color(0xFF0E2033),
)

/**
 * Дневной набор. Лайм из макета на белом не читается вовсе (1.3:1), а им
 * красится не только заливка кнопки, но и текст ссылок и активные подписи —
 * поэтому акцентом здесь работает тот же зелёный, но травяной, тёмный, а текст
 * на его заливке становится белым. Статусные цвета притемнены по той же
 * причине, подложки плашек — наоборот, высветлены.
 */
val ElectroLightColors = ElectroPalette(
    Background = Color(0xFFF2F5F7),
    Surface = Color(0xFFFFFFFF),
    SurfaceElevated = Color(0xFFE8EDF0),
    SurfacePressed = Color(0xFFDCE3E7),
    SurfaceRaised = Color(0xFFCFD8DE),
    Outline = Color(0xFFCBD4DA),

    Accent = Color(0xFF2F7D14),
    AccentPressed = Color(0xFF25620F),
    AccentSoft = Color(0xFFE3F5D7),
    OnAccent = Color(0xFFFFFFFF),

    TextPrimary = Color(0xFF0C1114),
    TextSecondary = Color(0xFF49535A),
    // 4.6:1 на Surface — проверено.
    TextMuted = Color(0xFF667079),
    TextDisabled = Color(0xFFA9B2B9),

    Ok = Color(0xFF15803D),
    Warn = Color(0xFF9A5B08),
    Danger = Color(0xFFC62828),
    Info = Color(0xFF1565C0),

    OkTint = Color(0xFFDFF3E5),
    WarnTint = Color(0xFFFAEEDB),
    DangerTint = Color(0xFFFBE4E2),
    InfoTint = Color(0xFFE1EDFA),
)

internal val LocalElectroColors = staticCompositionLocalOf { ElectroDarkColors }

/** Палитра, действующая на экране прямо сейчас. */
val ElectroColors: ElectroPalette
    @Composable @ReadOnlyComposable get() = LocalElectroColors.current
