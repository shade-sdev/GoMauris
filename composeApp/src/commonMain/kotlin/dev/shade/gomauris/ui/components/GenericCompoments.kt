package dev.shade.gomauris.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shade.gomauris.ui.theme.GoMaurisColors
import dev.shade.gomauris.ui.theme.RobotoFontFamily
import dev.shade.gomauris.ui.theme.selectionColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    icon: ImageVector,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier,
        color = GoMaurisColors.secondary,
        shape = RoundedCornerShape(4.dp),
    ) {
        CompositionLocalProvider(
            LocalTextSelectionColors provides selectionColors
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, bottom = 3.dp),
                textStyle = TextStyle(
                    fontFamily = RobotoFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = GoMaurisColors.scrim,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(Color.Black),
            ) { innerTextField ->

                TextFieldDefaults.DecorationBox(
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(
                            label,
                            fontSize = 14.sp,
                            fontFamily = RobotoFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = GoMaurisColors.surfaceTint,
                        )
                    },
                    value = value,
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = "",
                            tint = GoMaurisColors.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    enabled = true,
                    interactionSource = interactionSource,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledTextColor = Color.LightGray,
                        unfocusedContainerColor = Color.Black,
                        focusedContainerColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedLabelColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(0.dp),
                    container = {}
                )
            }
        }
    }
}