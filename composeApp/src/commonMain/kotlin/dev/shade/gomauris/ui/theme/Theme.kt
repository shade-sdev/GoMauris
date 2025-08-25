package dev.shade.gomauris.ui.theme

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.shade.gomauris.Res
import dev.shade.gomauris.aldrich_regular
import dev.shade.gomauris.roboto_bold
import dev.shade.gomauris.roboto_light
import dev.shade.gomauris.roboto_regular
import dev.shade.gomauris.russoone
import org.jetbrains.compose.resources.Font

val GoMaurisColors = lightColorScheme(
    primary = Color(0xFFFFFFFF), // White
    secondary = Color(0xFFFAFAFA), // Textbox
    tertiary = Color(0xFF737373), // Icon
    outline = Color(0xFFECECEC), // Border
    scrim = Color(0xFF222222), // Black
    surfaceBright = Color(0xFF06C961), // Selected
    surfaceTint = Color(0x0FFBABABA) // Icon lighter
)

val selectionColors = TextSelectionColors(
    handleColor = Color(0xFF4CAF50),
    backgroundColor = Color(0x664CAF50)
)

val RobotoFontFamily
    @Composable get() = FontFamily(
        Font(Res.font.roboto_regular, FontWeight.Normal),
        Font(Res.font.roboto_bold, FontWeight.Bold),
        Font(Res.font.roboto_light, FontWeight.Light)
    )


val AldrichFontFamily
    @Composable get() = FontFamily(
        Font(Res.font.aldrich_regular, FontWeight.Normal)
    )

val RussoOneFontFamily
    @Composable get() = FontFamily(
        Font(Res.font.russoone, FontWeight.Normal, FontStyle.Normal)
    )