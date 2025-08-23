package dev.shade.gomauris.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import dev.shade.gomauris.ui.theme.GoMaurisColors
import dev.shade.gomauris.ui.theme.RobotoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
val BottomNavigation: @Composable (Int, List<Tab>, (Int) -> Unit) -> Unit =
    { state, tabs, onTabSelected ->
        Column {
            HorizontalDivider(
                thickness = 1.dp,
                color = GoMaurisColors.outline,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryTabRow(
                selectedTabIndex = state,
                modifier = Modifier.fillMaxWidth(),
                containerColor = GoMaurisColors.primary,
                indicator = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = state == index,
                        onClick = { onTabSelected(index) },
                        icon = {
                            Icon(
                                tab.options.icon!!,
                                contentDescription = "Back",
                                tint = if (state == index) GoMaurisColors.surfaceBright else GoMaurisColors.surfaceTint,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        text = {
                            Text(
                                tab.options.title,
                                color = if (state == index) GoMaurisColors.surfaceBright
                                else GoMaurisColors.surfaceTint,
                                fontFamily = RobotoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    )
                }
            }
        }
    }