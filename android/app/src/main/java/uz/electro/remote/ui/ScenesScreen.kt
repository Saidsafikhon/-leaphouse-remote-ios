package uz.electro.remote.ui

import uz.electro.remote.ui.components.Lx
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import uz.electro.remote.data.Cmd
import uz.electro.remote.data.SceneStepDto
import uz.electro.remote.data.SceneTemplateDto
import uz.electro.remote.ui.components.*
import uz.electro.remote.ui.theme.*

/**
 * Пользовательские сцены: свой набор команд под одной кнопкой.
 *
 * Сцена живёт у пары «водитель + машина» на сервере, а не в телефоне: будильник
 * и привычки одного водителя не должны появляться у соседа по парку, а состав
 * сцены переживает переустановку приложения.
 *
 * Конструктор намеренно узкий: климат, температура и «остудить/прогреть»
 * шагами из проверенных команд. Собирать здесь весь кузов побайтно незачем —
 * это делает голова, а человеку нужны привычные наборы.
 */
@Composable
fun ScenesScreen(
    scenes: List<SceneTemplateDto>,
    onRun: (SceneTemplateDto) -> Unit,
    onDelete: (SceneTemplateDto) -> Unit,
    onCreate: (String, List<SceneStepDto>) -> Unit,
    onBack: () -> Unit,
) {
    var building by remember { mutableStateOf(false) }

    ScreenScaffold("Мои сцены", onBack) {
        if (scenes.isEmpty() && !building) {
            EmptyNote("Сцен пока нет. Соберите свою — например «остудить к выходу».")
        }
        scenes.forEach { scene ->
            SceneRow(scene, onRun = { onRun(scene) }, onDelete = { onDelete(scene) })
        }

        if (building) {
            SceneBuilder(
                onCancel = { building = false },
                onSave = { name, steps -> onCreate(name, steps); building = false },
            )
        } else {
            ElectroButton("Новая сцена", Modifier.fillMaxWidth(), style = ButtonStyle.Secondary) {
                building = true
            }
        }
    }
}

@Composable
private fun SceneRow(scene: SceneTemplateDto, onRun: () -> Unit, onDelete: () -> Unit) {
    SectionCard(scene.name) {
        Text(
            scene.steps.joinToString(" · ") { it.title.ifBlank { "тип ${it.type}" } },
            style = ElectroType.Caption, color = ElectroColors.TextMuted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Space.x2)) {
            ControlTile("Выполнить", Lx.PlayArrow, ControlState.Active,
                Modifier.weight(1f), onClick = onRun)
            ControlTile("Удалить", Lx.Delete, ControlState.Default,
                Modifier.weight(1f), onClick = onDelete)
        }
    }
}

/** Заготовки шагов: проверенные команды, из которых человек собирает сцену. */
private data class Ingredient(val title: String, val step: SceneStepDto)

private val INGREDIENTS = listOf(
    Ingredient("Включить климат", SceneStepDto(Cmd.AC, "1", "климат")),
    Ingredient("Выключить климат", SceneStepDto(Cmd.AC, "0", "климат выкл")),
    Ingredient("Тепло, 24°", SceneStepDto(Cmd.TEMP_L, "24", "температура 24°")),
    Ingredient("Прохладно, 20°", SceneStepDto(Cmd.TEMP_L, "20", "температура 20°")),
    Ingredient("Обдув на максимум", SceneStepDto(Cmd.FAN, "7", "обдув 7")),
    Ingredient("Открыть окна", SceneStepDto(Cmd.WINDOW_FL, "100", "окна открыть")),
    Ingredient("Закрыть окна", SceneStepDto(Cmd.WINDOW_FL, "0", "окна закрыть")),
)

@Composable
private fun SceneBuilder(
    onCancel: () -> Unit,
    onSave: (String, List<SceneStepDto>) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val chosen = remember { mutableStateListOf<Ingredient>() }

    SectionCard("Новая сцена") {
        OutlinedTextField(
            value = name, onValueChange = { name = it }, singleLine = true,
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectroColors.Accent,
                focusedLabelColor = ElectroColors.Accent,
                cursorColor = ElectroColors.Accent,
                focusedTextColor = ElectroColors.TextPrimary,
                unfocusedTextColor = ElectroColors.TextPrimary,
            ),
        )
        Text("Шаги", style = ElectroType.Caption, color = ElectroColors.TextSecondary)
        INGREDIENTS.forEach { ing ->
            val on = chosen.contains(ing)
            Row(
                Modifier.fillMaxWidth().clickable {
                    if (on) chosen.remove(ing) else chosen.add(ing)
                }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(ing.title, style = ElectroType.Body,
                    color = if (on) ElectroColors.Accent else ElectroColors.TextPrimary,
                    modifier = Modifier.weight(1f))
                if (on) Icon(Lx.Add, null, tint = ElectroColors.Accent,
                    modifier = Modifier.size(18.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.x2)) {
            ElectroButton("Отмена", Modifier.weight(1f), style = ButtonStyle.Ghost, onClick = onCancel)
            ElectroButton(
                "Сохранить", Modifier.weight(1f),
                style = ButtonStyle.Primary,
                enabled = name.isNotBlank() && chosen.isNotEmpty(),
            ) { onSave(name.trim(), chosen.map { it.step }) }
        }
    }
}
