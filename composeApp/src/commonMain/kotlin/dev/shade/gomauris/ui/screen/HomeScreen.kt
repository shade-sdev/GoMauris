package dev.shade.gomauris.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.tab.TabNavigator
import dev.shade.gomauris.ui.components.BottomNavigation
import dev.shade.gomauris.ui.screen.tabs.ActivityTab
import dev.shade.gomauris.ui.screen.tabs.HistoryTab
import dev.shade.gomauris.ui.screen.tabs.HomeTab
import dev.shade.gomauris.ui.theme.GoMaurisColors
import org.jetbrains.compose.ui.tooling.preview.Preview

class HomeScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Preview
    override fun Content() {
        var state by remember { mutableIntStateOf(0) }
        val tabs = listOf(HomeTab, ActivityTab, HistoryTab)

        TabNavigator(HomeTab) { tabNavigator ->
            Scaffold(
                containerColor = Color.Black,
                bottomBar = {
                    BottomNavigation(state, tabs) { newIndex ->
                        state = newIndex
                        tabNavigator.current = tabs[newIndex]
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .background(GoMaurisColors.primary)
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CurrentScreen()
                }
            }
        }
    }

}