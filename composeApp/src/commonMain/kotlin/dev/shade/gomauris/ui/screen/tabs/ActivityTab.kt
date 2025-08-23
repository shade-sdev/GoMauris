package dev.shade.gomauris.ui.screen.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object ActivityTab : Tab {
    @Composable
    override fun Content() {
        Text("Activity")
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Activity"
            val icon = rememberVectorPainter(Icons.Default.Map)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

}