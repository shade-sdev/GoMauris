package dev.shade.gomauris

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.shade.gomauris.di.dataModule
import dev.shade.gomauris.ui.screen.HomeScreen
import dev.shade.gomauris.ui.theme.GoMaurisColors
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(
        application = {
            modules(dataModule)
        }
    ) {
        MaterialTheme(colorScheme = GoMaurisColors) {
            Navigator(HomeScreen()) { navigator: Navigator ->
                SlideTransition(navigator)
            }
        }
    }
}