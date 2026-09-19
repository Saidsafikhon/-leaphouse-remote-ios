package uz.electro.remote.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import uz.electro.remote.ui.theme.*

/** Поле формы входа: подпись сверху, тёмная заливка, радиус из системы. */
@Composable
internal fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType,
    visual: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    Text(label, color = ElectroColors.TextSecondary, fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(Space.x1))
    OutlinedTextField(
        value = value, onValueChange = onChange, singleLine = true,
        visualTransformation = visual,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        trailingIcon = trailing,
        shape = Radius.Sm,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ElectroColors.Surface,
            unfocusedContainerColor = ElectroColors.Surface,
            focusedBorderColor = ElectroColors.Accent,
            unfocusedBorderColor = ElectroColors.Outline,
            focusedTextColor = ElectroColors.TextPrimary,
            unfocusedTextColor = ElectroColors.TextPrimary,
            cursorColor = ElectroColors.Accent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Пояснение под полем: чего от него ждут и почему. */
@Composable
internal fun Hint(text: String) {
    Text(text, color = ElectroColors.TextMuted, fontSize = 12.sp)
}
