package dev.shade.gomauris

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
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
import dev.sargunv.maplibrecompose.expressions.dsl.asString
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.feature
import dev.sargunv.maplibrecompose.expressions.dsl.format
import dev.sargunv.maplibrecompose.expressions.dsl.image
import dev.sargunv.maplibrecompose.expressions.dsl.offset
import dev.sargunv.maplibrecompose.expressions.dsl.span
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import dev.sargunv.maplibrecompose.expressions.value.LineJoin
import dev.shade.gomauris.font.GoMaurisTheme
import dev.shade.gomauris.font.RobotoFontFamily
import dev.shade.gomauris.font.iconColor
import dev.shade.gomauris.font.lightGray
import dev.shade.gomauris.font.outlineVariant
import dev.shade.gomauris.font.primary
import dev.shade.gomauris.font.selectionColors
import dev.shade.gomauris.font.textBoxBackground
import dev.shade.gomauris.font.textForeground
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val STYLE = "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun App(
    routeCoordinates: List<Position>,
    source: Position?,
    destination: Position?
) {
    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(latitude = -20.200, longitude = 57.530),
            zoom = 11.0
        )
    )
    var selectedFeature by mutableStateOf<Feature?>(null)

    MaplibreMap(
        baseStyle = BaseStyle.Uri(STYLE),
        options = MapOptions(
            gestureOptions = GestureOptions.Standard,
            ornamentOptions = OrnamentOptions.AllDisabled
        ),
        cameraState = camera,
        onMapLongClick = { pos, _ ->
            println(pos.latitude)
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

        source?.let {
            val markerFeature = Feature(Point(it))
            val markerGeoJson = GeoJsonData.Features(markerFeature)
            val markerSource = rememberGeoJsonSource(markerGeoJson)

            SymbolLayer(
                id = "source",
                source = markerSource,
                onClick = { features ->
                    selectedFeature = features.firstOrNull()
                    ClickResult.Consume
                },
                iconImage = image(rememberTintedVectorPainter(Icons.Filled.LocationOn, Color.Blue)),
                textField =
                    format(
                        span(image("railway")),
                        span(" "),
                        span(
                            feature["STNCODE"].asString(),
                            textSize = const(1.2f.em)
                        ),
                    ),
                textFont = const(listOf("Noto Sans Regular")),
                textColor = const(MaterialTheme.colorScheme.onBackground),
                textOffset = offset(0.em, 0.6.em),
            )
        }

        destination?.let {
            val markerFeature = Feature(Point(it))
            val markerGeoJson = GeoJsonData.Features(markerFeature)
            val markerSource = rememberGeoJsonSource(markerGeoJson)

            SymbolLayer(
                id = "destination",
                source = markerSource,
                onClick = { features ->
                    selectedFeature = features.firstOrNull()
                    ClickResult.Consume
                },
                iconImage = image(
                    rememberTintedVectorPainter(
                        Icons.Filled.AddLocationAlt,
                        Color.Blue
                    )
                ),
                textField =
                    format(
                        span(image("railway")),
                        span(" "),
                        span(
                            feature["STNCODE"].asString(),
                            textSize = const(1.2f.em)
                        ),
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
fun SearchBottomSheet(
    onDismiss: () -> Unit,
    onPlaceSelected: (PhotonPlace) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false // Allow partial expansion
    )
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PhotonPlace>>(emptyList()) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                        color = iconColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column() {

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
                    .padding(PaddingValues(20.dp, 0.dp, 20.dp, 5.dp))
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormTextField(
                    value = "",
                    onValueChange = { },
                    label = "Enter Location",
                    icon = Icons.Outlined.LocationOn,
                    modifier = Modifier.height(40.dp).fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.weight(1f)
                    .padding(PaddingValues(20.dp, 0.dp, 20.dp, 5.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(15) { place ->
                        LocationItem()
                    }
                }
            }

        }
    }
}

@Composable
fun MapWithSearchSheet() {
    GoMaurisTheme {
        var showSheet by remember { mutableStateOf(true) }
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

        LaunchedEffect(destination) {
            routeCoordinates = fetchRouteFromOSRM(source, destination)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            App(routeCoordinates, source, destination)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(
                    onClick = { showSheet = true },
                    colors = IconButtonColors(
                        containerColor = outlineVariant,
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

            if (showSheet) {
                SearchBottomSheet(
                    onDismiss = { showSheet = false },
                    onPlaceSelected = { place ->
                        destination = Position(latitude = place.lat, longitude = place.lon)
                        showSheet = false
                    }
                )
            }
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

@Composable
@Preview
fun LocationItem() {
    Row(
        modifier = Modifier
    ) {

        Row(modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
            ) {
            Column(
                modifier = Modifier.wrapContentWidth()
                    .padding(PaddingValues(0.dp, 5.dp, 0.dp, 5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            width = 0.5.dp,
                            color = lightGray,
                            shape = CircleShape
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        tint = iconColor,
                        contentDescription = "icon"
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Town of Curepipe, Ward 5",
                    fontFamily = RobotoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "Curepipe",
                    fontFamily = RobotoFontFamily,
                    fontWeight = FontWeight.Light,
                    color = primary,
                    fontSize = 12.sp
                )
            }

        }
    }

    Row(modifier = Modifier.wrapContentHeight()) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = lightGray,
                modifier = Modifier.height(1.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    icon: ImageVector,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier,
        color = textBoxBackground,
        shape = RoundedCornerShape(4.dp),
    ) {
        CompositionLocalProvider(
            LocalTextSelectionColors provides selectionColors
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, bottom = 3.dp),
                textStyle = TextStyle(
                    fontFamily = RobotoFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = textForeground,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(Color.Black),
            ) { innerTextField ->

                TextFieldDefaults.DecorationBox(
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(
                            label,
                            fontSize = 14.sp,
                            fontFamily = RobotoFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = textForeground,
                        )
                    },
                    value = value,
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = "",
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    enabled = true,
                    interactionSource = interactionSource,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledTextColor = Color.LightGray,
                        unfocusedContainerColor = Color.Black,
                        focusedContainerColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedLabelColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(0.dp),
                    container = {}
                )
            }
        }
    }
}

private val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun fetchRouteFromOSRM(start: Position, end: Position): List<Position> =
    withContext(Dispatchers.IO) {
        val url =
            "https://router.project-osrm.org/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}" +
                    "?overview=full&geometries=geojson"

        val responseText: String = httpClient.get(url).bodyAsText()

        val json = Json.parseToJsonElement(responseText).jsonObject
        val routes = json["routes"]!!.jsonArray
        if (routes.isEmpty()) return@withContext emptyList()

        val coordinatesJson = routes[0].jsonObject["geometry"]!!
            .jsonObject["coordinates"]!!.jsonArray

        coordinatesJson.map { coordArray ->
            val coord = coordArray.jsonArray
            Position(
                latitude = coord[1].jsonPrimitive.double,
                longitude = coord[0].jsonPrimitive.double
            )
        }
    }

data class PhotonPlace(val name: String, val lat: Double, val lon: Double)

suspend fun searchPhoton(query: String): List<PhotonPlace> = try {
    val url = "https://photon.komoot.io/api/?q=${query}&limit=5&bbox=57.3,-20.5,57.8,-19.9"

    val responseText: String = httpClient.get(url) {
        headers {
            append(HttpHeaders.Accept, "application/json")
            append(HttpHeaders.UserAgent, "YourAppName")
        }
    }.bodyAsText()

    val json = Json.parseToJsonElement(responseText).jsonObject
    val features = json["features"]?.jsonArray ?: JsonArray(emptyList())

    features.map { featureElement ->
        val feature = featureElement.jsonObject
        val properties = feature["properties"]!!.jsonObject
        val geometry = feature["geometry"]!!.jsonObject
        val coords = geometry["coordinates"]!!.jsonArray

        PhotonPlace(
            name = properties["name"]?.jsonPrimitive?.content ?: query,
            lat = coords[1].jsonPrimitive.double,
            lon = coords[0].jsonPrimitive.double
        )
    }

} catch (e: Exception) {
    e.printStackTrace()
    emptyList()
}

//Column(
//modifier = Modifier
//.fillMaxSize()
//.padding(16.dp)
//) {
//    OutlinedTextField(
//        value = query,
//        onValueChange = { value ->
//            query = value
//            scope.launch {
//                results = if (value.isNotBlank()) {
//                    searchPhoton(value)
//                } else emptyList()
//            }
//        },
//        placeholder = { Text("Search destination...") },
//        modifier = Modifier.fillMaxWidth()
//    )
//
//    Spacer(Modifier.height(16.dp))
//
//    LazyColumn(
//        modifier = Modifier.weight(1f)
//    ) {
//        items(results) { place ->
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 4.dp)
//                    .clickable {
//                        onPlaceSelected(place)
//                        onDismiss()
//                    },
//                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//            ) {
//                Text(
//                    text = place.name,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
//        }
//    }
//
//    Spacer(Modifier.height(16.dp))
//}