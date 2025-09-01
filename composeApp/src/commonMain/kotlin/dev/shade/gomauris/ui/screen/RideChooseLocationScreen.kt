package dev.shade.gomauris.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import core.models.UiState
import dev.shade.gomauris.core.model.map.DetailedPosition
import dev.shade.gomauris.core.model.map.MapPointerStatus
import dev.shade.gomauris.ui.components.FormTextField
import dev.shade.gomauris.ui.components.LoadingScreen
import dev.shade.gomauris.ui.theme.GoMaurisColors
import dev.shade.gomauris.ui.theme.RobotoFontFamily
import dev.shade.gomauris.viewmodel.HomeTabViewModel
import kotlinx.coroutines.FlowPreview

class RideChooseLocationScreen(private val screenModel: HomeTabViewModel) : Screen {

    @Composable
    override fun Content() {

        MapBottomSheet(screenModel)
    }

}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MapBottomSheet(screenModel: HomeTabViewModel) {
    val modelSourceSearch by screenModel.sourceSearch.collectAsState()
    val modelDestinationSearch by screenModel.destinationSearch.collectAsState()

    val destinationFocusRequester = remember { FocusRequester() }

    Column {
        Row(
            modifier = Modifier.wrapContentHeight()
                .padding(PaddingValues(20.dp, 10.dp, 20.dp, 10.dp))
        ) {
            Text(
                text = "Where to?",
                color = Color.Black,
                fontFamily = RobotoFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier.wrapContentHeight()
                .padding(PaddingValues(20.dp, 0.dp, 20.dp, 10.dp))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormTextField(
                value = modelSourceSearch ?: "",
                onValueChange = { screenModel.updateSourceSearch(it) },
                label = "From Where?",
                icon = Icons.Outlined.LocationOn,
                iconColor = GoMaurisColors.surfaceBright,
                modifier = Modifier.height(40.dp)
                    .fillMaxWidth()
                    .focusRequester(destinationFocusRequester)
                    .onFocusChanged() {
                        if (it.isFocused) {
                            screenModel.updateSelectedSearchField(MapPointerStatus.SOURCE)
                        }
                    }
            )
        }

        Row(
            modifier = Modifier.wrapContentHeight()
                .padding(PaddingValues(20.dp, 0.dp, 20.dp, 5.dp))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FormTextField(
                value = modelDestinationSearch ?: "",
                onValueChange = { screenModel.updateDestinationSearch(it) },
                label = "Where to?",
                icon = Icons.Outlined.AddLocationAlt,
                iconColor = Color.Red,
                modifier = Modifier.height(40.dp)
                    .fillMaxWidth()
                    .focusRequester(destinationFocusRequester)
                    .onFocusChanged() {
                        if (it.isFocused) {
                            screenModel.updateSelectedSearchField(MapPointerStatus.DESTINATION)
                        }
                    }
            )
        }

        Row(
            modifier = Modifier.weight(1f)
                .padding(PaddingValues(20.dp, 0.dp, 20.dp, 5.dp))
        ) {
            MapBottomSheetBottomContent(screenModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBottomSheetBottomContent(
    screenModel: HomeTabViewModel
) {
    val results by screenModel.locationResults.collectAsState()
    val focusManager = LocalFocusManager.current

    when (val state = results) {
        is UiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(PaddingValues(top = 40.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Something went wrong try again",
                    fontSize = 14.sp,
                    fontFamily = RobotoFontFamily,
                    fontWeight = FontWeight.Light,
                    color = GoMaurisColors.surfaceTint
                )
            }
        }

        UiState.Idle -> {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(PaddingValues(top = 40.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Search to get started",
                    fontSize = 14.sp,
                    fontFamily = RobotoFontFamily,
                    fontWeight = FontWeight.Light,
                    color = GoMaurisColors.surfaceTint
                )
            }
        }

        UiState.Loading -> {
            LoadingScreen(
                backgroundColor = Color.White,
                textColor = GoMaurisColors.surfaceTint,
                indicatorColor = GoMaurisColors.surfaceBright,
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.padding(PaddingValues(top = 20.dp)),
                trackColor = GoMaurisColors.secondary
            )
        }

        is UiState.Success<List<DetailedPosition>> -> {
            if (screenModel.isLocationChosen()) {
                screenModel.swipeDisabled()
                ShowActionButton(screenModel)
            } else {
                LazyColumn {
                    items(state.data) { place ->
                        LocationItem(
                            place.name,
                            place.displayName,
                            Modifier
                                .clickable {
                                    screenModel.locationItemClick(place)
                                    focusManager.clearFocus()
                                }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowActionButton(screenModel: HomeTabViewModel) {
    val bottomSheetNavigator = LocalNavigator.current

    Row(
        modifier = Modifier.fillMaxSize()
            .padding(
                PaddingValues(
                    top = LocalWindowInfo.current.containerSize.height.dp.div(
                        11f
                    )
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Button(
            onClick = { screenModel.resetLocationChoice() },
            modifier = Modifier.width(150.dp),
            colors = ButtonColors(
                containerColor = GoMaurisColors.surfaceTint,
                contentColor = Color.White,
                disabledContentColor = GoMaurisColors.surfaceTint,
                disabledContainerColor = GoMaurisColors.surfaceTint,
            )
        )
        {
            Text(
                color = Color.White,
                text = "Cancel",
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                fontFamily = RobotoFontFamily
            )
        }
        Button(
            onClick = {
                screenModel.setSheetValue(SheetValue.Expanded)
                bottomSheetNavigator?.push(RideChooseTimeScreen(screenModel))
            },
            modifier = Modifier.width(150.dp),
            colors = ButtonColors(
                containerColor = GoMaurisColors.surfaceBright,
                contentColor = Color.White,
                disabledContentColor = GoMaurisColors.surfaceBright,
                disabledContainerColor = GoMaurisColors.surfaceBright,
            )
        )
        {
            Text(
                color = Color.White,
                text = "Next",
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = RobotoFontFamily
            )
        }
    }
}

@Composable
fun LocationItem(
    name: String?,
    displayName: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        width = 0.5.dp,
                        color = GoMaurisColors.outline,
                        shape = CircleShape
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    tint = GoMaurisColors.tertiary,
                    contentDescription = "icon"
                )
            }

            Column(
                modifier = Modifier.fillMaxSize()
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = name ?: "",
                    fontFamily = RobotoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = displayName ?: "-",
                    fontFamily = RobotoFontFamily,
                    fontWeight = FontWeight.Light,
                    color = GoMaurisColors.scrim,
                    fontSize = 12.sp
                )
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = GoMaurisColors.surfaceTint,
            modifier = Modifier.fillMaxWidth()
        )
    }
}