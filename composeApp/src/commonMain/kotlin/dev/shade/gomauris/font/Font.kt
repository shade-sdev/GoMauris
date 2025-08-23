package dev.shade.gomauris.font

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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

val primary = Color(0xFF191919)
val secondary = Color(0xFF121212)
val tertiary = Color(0xFF0A0A0A)
val onSurface = Color(0xFF292929)
val outline = Color(0xFFBABABA)
val outlineVariant = Color(0xFF8592A5)
val orange = Color(0xFFFB8C00)
val surfaceContainer = Color(0xFF333333)
val primaryContainer = Color(0xFF2B2B2B)
val scrim = Color(0xFFCD9B5B)
val surfaceBright = Color(0xFF9AE6B4)
val surfaceContainerLow = Color(0xFF6C6C6C)
val inverseOnSurface = Color(0xFFE25C63)
val onTertiaryContainer = Color(0xFF1C1520)
val inversePrimary = Color(0xFFFF6363)

val textBoxBackground = Color(0xFFFAFAFa)
val miscBackground = Color(0xFF09C25C)
val textForeground = Color(0xFFC5C5C5)
val iconColor = Color(0xFF747474)
val lightGray = Color(0xFFF5F5F5)

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

@Composable
private fun interTypography(): Typography {
    val interFont = FontFamily(
        Font(Res.font.russoone, FontWeight.Normal),
        Font(Res.font.russoone, FontWeight.Bold),
    )

    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(fontFamily = interFont, fontWeight = FontWeight.Bold),
            displayMedium = displayMedium.copy(
                fontFamily = interFont,
                fontWeight = FontWeight.Bold
            ),
            displaySmall = displaySmall.copy(fontFamily = interFont, fontWeight = FontWeight.Bold),
            headlineLarge = headlineLarge.copy(
                fontFamily = interFont,
                fontWeight = FontWeight.Bold
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = interFont,
                fontWeight = FontWeight.Bold
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = interFont,
                fontWeight = FontWeight.Bold
            ),
            titleLarge = titleLarge.copy(fontFamily = interFont, fontWeight = FontWeight.Bold),
            titleMedium = titleMedium.copy(fontFamily = interFont, fontWeight = FontWeight.Bold),
            titleSmall = titleSmall.copy(fontFamily = interFont, fontWeight = FontWeight.Bold),
            labelLarge = labelLarge.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            labelMedium = labelMedium.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            labelSmall = labelSmall.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            bodyLarge = bodyLarge.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            bodyMedium = bodyMedium.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
            bodySmall = bodySmall.copy(fontFamily = interFont, fontWeight = FontWeight.Normal),
        )
    }
}

@Composable
fun GoMaurisTheme(content: @Composable () -> Unit) {
    MaterialTheme(typography = interTypography(), content = content)
}