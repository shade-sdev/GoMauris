package dev.shade.gomauris.ui.screen.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.shade.gomauris.App
import io.github.dellisd.spatialk.geojson.Position

object HomeTab : Tab {

    @Composable
    override fun Content() {
        var destination by remember {
            mutableStateOf(
                Position(
                    latitude = -20.1669,
                    longitude = 57.5023
                )
            )
        }
        val source = Position(latitude = -20.24444, longitude = 57.55417)
        var routeCoordinates by remember { mutableStateOf<List<Position>>(emptyList()) }
        App(routeCoordinates, source, destination)
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Home"
            val icon = rememberVectorPainter(Icons.Default.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }
}