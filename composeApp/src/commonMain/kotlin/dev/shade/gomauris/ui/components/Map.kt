package dev.shade.gomauris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.sargunv.maplibrecompose.compose.ClickResult
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.BaseStyle
import dev.sargunv.maplibrecompose.core.CameraPosition
import dev.sargunv.maplibrecompose.core.GestureOptions
import dev.sargunv.maplibrecompose.core.MapOptions
import dev.sargunv.maplibrecompose.core.OrnamentOptions
import dev.sargunv.maplibrecompose.core.source.GeoJsonData
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.image
import dev.sargunv.maplibrecompose.expressions.dsl.offset
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import dev.sargunv.maplibrecompose.expressions.value.LineJoin
import dev.shade.gomauris.core.model.MapPointerStatus
import dev.shade.gomauris.ui.theme.GoMaurisColors
import dev.shade.gomauris.ui.theme.RobotoFontFamily
import dev.shade.gomauris.viewmodel.HomeTabViewModel
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import kotlinx.coroutines.delay

@Composable
fun GoMaurisMapContainer(screenModel: HomeTabViewModel) {
    val sheetState by screenModel.sheetState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GoMaurisMap(screenModel)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = { screenModel.toggleSheet() },
                colors = IconButtonColors(
                    containerColor = GoMaurisColors.tertiary,
                    contentColor = Color.White,
                    disabledContentColor = Color.LightGray,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Icon(
                    Icons.Sharp.Search,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp),
                    contentDescription = "Search",
                )
            }
        }

        if (sheetState) {
            MapBottomSheet(screenModel)
        }
    }

}

@Composable
fun GoMaurisMap(
    screenModel: HomeTabViewModel
) {
    val source by screenModel.source.collectAsState()
    val destination by screenModel.destination.collectAsState()

    val routeCoordinates by screenModel.routeCoordinates.collectAsState()

    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(latitude = -20.200, longitude = 57.530),
            zoom = 11.0
        )
    )

    MaplibreMap(
        baseStyle = BaseStyle.Uri(HomeTabViewModel.STYLE),
        options = MapOptions(
            gestureOptions = GestureOptions.Standard,
            ornamentOptions = OrnamentOptions.AllDisabled
        ),
        cameraState = camera,
        onMapLongClick = { pos, _ ->
            screenModel.mapClick(pos)
            ClickResult.Consume
        }
    ) {
        if (routeCoordinates.isNotEmpty()) {

            val routeLineString = LineString(routeCoordinates)
            val routeFeature = Feature(routeLineString)
            val routeGeoJson = GeoJsonData.Features(routeFeature)
            val routeSource = rememberGeoJsonSource(routeGeoJson)

            LineLayer(
                id = "route",
                source = routeSource,
                color = const(Color(0xFF1E90FF)),
                width = const(4.dp),
                cap = const(LineCap.Round),
                join = const(LineJoin.Round)
            )
        }

        source.position?.let {
            val markerFeature = Feature(Point(it))
            val markerGeoJson = GeoJsonData.Features(markerFeature)
            val markerSource = rememberGeoJsonSource(markerGeoJson)

            SymbolLayer(
                id = "source",
                source = markerSource,
                onClick = { features ->
                    ClickResult.Consume
                },
                iconImage = image(
                    rememberTintedVectorPainter(
                        Icons.Filled.LocationOn,
                        GoMaurisColors.surfaceBright
                    )
                ),
                textFont = const(listOf("Noto Sans Regular")),
                textColor = const(MaterialTheme.colorScheme.onBackground),
                textOffset = offset(0.em, 0.6.em),
            )
        }

        destination.position?.let {
            val markerFeature = Feature(Point(it))
            val markerGeoJson = GeoJsonData.Features(markerFeature)
            val markerSource = rememberGeoJsonSource(markerGeoJson)

            SymbolLayer(
                id = "destination",
                source = markerSource,
                onClick = { features ->
                    ClickResult.Consume
                },
                iconImage = image(
                    rememberTintedVectorPainter(
                        Icons.Filled.AddLocationAlt,
                        Color.Red
                    )
                ),
                textFont = const(listOf("Noto Sans Regular")),
                textColor = const(MaterialTheme.colorScheme.onBackground),
                textOffset = offset(0.em, 0.6.em),
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBottomSheet(screenModel: HomeTabViewModel) {
    val results by screenModel.locationResults.collectAsState()

    val modelSourceSearch by screenModel.sourceSearch.collectAsState()
    val modelDestinationSearch by screenModel.destinationSearch.collectAsState()

    var sourceSearch by remember(modelSourceSearch) {
        mutableStateOf(modelSourceSearch ?: "")
    }

    var destinationSearch by remember(modelDestinationSearch) {
        mutableStateOf(modelDestinationSearch ?: "")
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    LaunchedEffect(sourceSearch) {
        if (sourceSearch.isNotEmpty()) {
            delay(600)
            screenModel.updateSourceSearch(sourceSearch)
        }
    }

    LaunchedEffect(destinationSearch) {
        if (destinationSearch.isNotEmpty()) {
            delay(600)
            screenModel.updateDestinationSearch(destinationSearch)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { screenModel.toggleSheet() },
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(),
        shape = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp
        ),
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(32.dp)
                    .height(3.dp)
                    .background(
                        color = GoMaurisColors.tertiary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
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
                    value = sourceSearch,
                    onValueChange = { sourceSearch = it },
                    label = "From Where?",
                    icon = Icons.Outlined.LocationOn,
                    iconColor = GoMaurisColors.surfaceBright,
                    modifier = Modifier.height(40.dp)
                        .fillMaxWidth()
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
                    value = destinationSearch,
                    onValueChange = { destinationSearch = it },
                    label = "Where to?",
                    icon = Icons.Outlined.AddLocationAlt,
                    iconColor = Color.Red,
                    modifier = Modifier.height(40.dp)
                        .fillMaxWidth()
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
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(results) { place ->
                        LocationItem(
                            place.name,
                            place.displayName,
                            Modifier.clickable { screenModel.locationItemClick(place) }
                        )
                    }
                }
            }

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
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
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
                modifier = Modifier
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

@Composable
fun rememberTintedVectorPainter(
    imageVector: ImageVector,
    tint: Color
): Painter {
    val originalPainter = rememberVectorPainter(imageVector)
    return remember(imageVector, tint) {
        object : Painter() {
            override val intrinsicSize: Size = originalPainter.intrinsicSize
            override fun DrawScope.onDraw() {
                with(originalPainter) {
                    draw(
                        size = size,
                        colorFilter = ColorFilter.tint(tint)
                    )
                }
            }
        }
    }
}