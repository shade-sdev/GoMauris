package dev.shade.gomauris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
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
import dev.shade.gomauris.ui.screen.RideChooseLocationScreen
import dev.shade.gomauris.ui.theme.GoMaurisColors
import dev.shade.gomauris.viewmodel.HomeTabViewModel
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GoMaurisMapContainer(screenModel: HomeTabViewModel) {
    val sheetValue by screenModel.sheetValue.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            skipPartiallyExpanded = false,
            initialValue = sheetValue,
            density = LocalDensity.current,
            confirmValueChange = { true },
            skipHiddenState = false
        )
    )

    val scope = rememberCoroutineScope()
    val swipeEnabled = screenModel.bottomSheetSwipeEnable.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = LocalWindowInfo.current.containerSize.height.dp.div(5.5f),
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            sheetContainerColor = Color.White,
            sheetContent = {
                Navigator(RideChooseLocationScreen(screenModel)) { navigator: Navigator ->
                    SlideTransition(navigator)
                }
            },
            sheetSwipeEnabled = swipeEnabled.value,
            sheetDragHandle = {
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
        ) { paddingValues ->

            Box(
                modifier = Modifier.fillMaxSize()
            )
            {
                GoMaurisMap(screenModel)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (scaffoldState.bottomSheetState.isVisible) {
                                    scaffoldState.bottomSheetState.hide()
                                } else {
                                    scaffoldState.bottomSheetState.partialExpand()
                                }
                            }
                        },
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

            }
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