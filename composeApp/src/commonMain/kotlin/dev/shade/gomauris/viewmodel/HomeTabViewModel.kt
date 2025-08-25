package dev.shade.gomauris.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.shade.gomauris.core.model.DetailedPosition
import dev.shade.gomauris.core.model.GoMaurisEvent
import dev.shade.gomauris.core.model.MapPointerStatus
import dev.shade.gomauris.core.model.OpenStreetMap
import dev.shade.gomauris.httpClient
import io.github.dellisd.spatialk.geojson.Position
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HomeTabViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    companion object {
        const val STYLE: String = "https://tiles.openfreemap.org/styles/liberty"
    }

    private val eventChannel = Channel<GoMaurisEvent>(Channel.BUFFERED)

    private val _sheetState = MutableStateFlow(true)
    val sheetState: StateFlow<Boolean> = _sheetState.asStateFlow()

    private val _source = MutableStateFlow(DetailedPosition(null, null, null))
    val source: StateFlow<DetailedPosition> = _source.asStateFlow()

    private val _destination = MutableStateFlow(DetailedPosition(null, null, null))
    val destination: StateFlow<DetailedPosition> = _destination.asStateFlow()

    private val _routeCoordinates = MutableStateFlow<List<Position>>(emptyList())
    val routeCoordinates: StateFlow<List<Position>> = _routeCoordinates.asStateFlow()

    private val _mapPointerStatus = MutableStateFlow(MapPointerStatus.NONE)
    val mapPointerStatus: StateFlow<MapPointerStatus> = _mapPointerStatus.asStateFlow()

    private val _locationResults = MutableStateFlow<List<DetailedPosition>>(emptyList())
    val locationResults: StateFlow<List<DetailedPosition>> = _locationResults.asStateFlow()

    private val _sourceSearch = MutableStateFlow<String?>(null)
    val sourceSearch: StateFlow<String?> = _sourceSearch.asStateFlow()

    init {
        handleEvents()
    }

    fun toggleSheet() {
        _sheetState.value = !_sheetState.value
    }

    fun mapClick(position: Position) {
        when (_mapPointerStatus.value) {
            MapPointerStatus.NONE -> {
                _source.value = _source.value.copy(position = position)
                geoDecode(_source)
                _mapPointerStatus.value = MapPointerStatus.SOURCE
            }

            MapPointerStatus.SOURCE -> {
                _destination.value = _destination.value.copy(position = position)
                geoDecode(_destination)
                fetchRouteFromOSRM()
                _mapPointerStatus.value = MapPointerStatus.DESTINATION
            }

            MapPointerStatus.DESTINATION -> {
                _source.value = DetailedPosition(null, null, null)
                _destination.value = DetailedPosition(null, null, null)
                _routeCoordinates.value = emptyList()
                _mapPointerStatus.value = MapPointerStatus.NONE
            }
        }
    }

    fun fetchRouteFromOSRM() {
        viewModelScope.launch(dispatcher) {
            val url =
                "https://router.project-osrm.org/route/v1/driving/${_source.value.position?.longitude},${_source.value.position?.latitude};${_destination.value.position?.longitude},${_destination.value.position?.latitude}" +
                        "?overview=full&geometries=geojson"

            val responseText: String = httpClient.get(url).bodyAsText()
            val json = Json.parseToJsonElement(responseText).jsonObject
            val routes = json["routes"]!!.jsonArray

            if (routes.isEmpty())
                return@launch

            val coordinatesJson = routes[0].jsonObject["geometry"]!!
                .jsonObject["coordinates"]!!.jsonArray

            _routeCoordinates.value = coordinatesJson.map { coordArray ->
                val coord = coordArray.jsonArray
                Position(
                    latitude = coord[1].jsonPrimitive.double,
                    longitude = coord[0].jsonPrimitive.double
                )
            }
        }
    }

    fun geoDecode(position: MutableStateFlow<DetailedPosition>) {
        viewModelScope.launch(dispatcher) {
            val url =
                "https://nominatim.openstreetmap.org/reverse?lat=${position.value.position?.latitude}&lon=${position.value.position?.longitude}&zoom=18&format=jsonv2"
            val location: OpenStreetMap = httpClient.get(url).body()
            position.value = position.value.copy(
                name = location.name, displayName = location.display_name
            )
        }
    }

    fun geoDecode(location: String) {
        println("CALL")
        viewModelScope.launch(dispatcher) {
            val url =
                "https://nominatim.openstreetmap.org/search?q=${location}&countrycodes=mu&format=jsonv2"
            val locations: List<OpenStreetMap> = httpClient.get(url).body()
            _locationResults.value = locations.map { it ->
                DetailedPosition(
                    Position(it.lon, it.lat, it.lon),
                    it.name, it.display_name)
            }
        }
    }

    fun updateSourceSearch(search: String) {
        _sourceSearch.value = search
        viewModelScope.launch(dispatcher) {
            eventChannel.send(GoMaurisEvent.SourceSearchChanged(search))
        }
    }

    private fun handleEvents() {
        viewModelScope.launch(dispatcher) {
            eventChannel.consumeAsFlow().collect { event ->
                when (event) {
                    is GoMaurisEvent.SourceSearchChanged -> {
                        geoDecode(event.search)
                    }
                }
            }
        }
    }

}