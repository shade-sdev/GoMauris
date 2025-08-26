package dev.shade.gomauris.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.shade.gomauris.core.model.DetailedPosition
import dev.shade.gomauris.core.model.MapPointerStatus
import dev.shade.gomauris.core.model.OpenStreetMap
import dev.shade.gomauris.httpClient
import io.github.dellisd.spatialk.geojson.Position
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _sheetState = MutableStateFlow(true)
    val sheetState: StateFlow<Boolean> = _sheetState.asStateFlow()

    private val _source = MutableStateFlow(DetailedPosition(null, null, null))
    val source: StateFlow<DetailedPosition> = _source.asStateFlow()

    private val _destination = MutableStateFlow(DetailedPosition(null, null, null))
    val destination: StateFlow<DetailedPosition> = _destination.asStateFlow()

    private val _routeCoordinates = MutableStateFlow<List<Position>>(emptyList())
    val routeCoordinates: StateFlow<List<Position>> = _routeCoordinates.asStateFlow()

    private val _mapPointerStatus = MutableStateFlow(MapPointerStatus.NONE)

    private val _locationResults = MutableStateFlow<List<DetailedPosition>>(emptyList())
    val locationResults: StateFlow<List<DetailedPosition>> = _locationResults.asStateFlow()

    private val _sourceSearch = MutableStateFlow<String?>(null)
    val sourceSearch: StateFlow<String?> = _sourceSearch.asStateFlow()

    private val _destinationSearch = MutableStateFlow<String?>(null)
    val destinationSearch: StateFlow<String?> = _destinationSearch.asStateFlow()

    private val _selectedTextField = MutableStateFlow(MapPointerStatus.NONE)

    private val sourceSearchChannel = Channel<String>(Channel.UNLIMITED)
    private val destinationSearchChannel = Channel<String>(Channel.UNLIMITED)

    init {
        observeSourceSearch()
        observeDestinationSearch()
    }

    fun toggleSheet() {
        _sheetState.value = !_sheetState.value
    }

    fun mapClick(position: Position) {
        when (_mapPointerStatus.value) {
            MapPointerStatus.NONE -> {
                _source.value = _source.value.copy(position = position)
                geoDecode(_source, MapPointerStatus.SOURCE)
                _mapPointerStatus.value = MapPointerStatus.SOURCE
            }

            MapPointerStatus.SOURCE -> {
                _destination.value = _destination.value.copy(position = position)
                geoDecode(_destination, MapPointerStatus.DESTINATION)
                fetchRouteFromOSRM()
                _mapPointerStatus.value = MapPointerStatus.DESTINATION
            }

            MapPointerStatus.DESTINATION -> {
                _source.value = DetailedPosition(null, null, null)
                _destination.value = DetailedPosition(null, null, null)
                _sourceSearch.value = null
                _destinationSearch.value = null
                _locationResults.value = emptyList()
                _routeCoordinates.value = emptyList()
                _mapPointerStatus.value = MapPointerStatus.NONE
            }

            MapPointerStatus.DESTINATION_WITHOUT_SOURCE -> {
                _source.value = _source.value.copy(position = position)
                geoDecode(_source, MapPointerStatus.DESTINATION_WITHOUT_SOURCE)
                fetchRouteFromOSRM()
                _mapPointerStatus.value = MapPointerStatus.DESTINATION
            }
        }
    }

    fun locationItemClick(place: DetailedPosition) {
        when (_mapPointerStatus.value) {
            MapPointerStatus.NONE -> {
                when (_selectedTextField.value) {
                    MapPointerStatus.NONE -> {}
                    MapPointerStatus.SOURCE -> {
                        _source.value = place
                        _sourceSearch.value =
                            place.displayName?.takeIf { it.isNotBlank() } ?: place.name
                        if (_destination.value.position != null) {
                            _mapPointerStatus.value = MapPointerStatus.DESTINATION
                            fetchRouteFromOSRM()
                        } else {
                            _mapPointerStatus.value = MapPointerStatus.SOURCE
                        }
                    }

                    MapPointerStatus.DESTINATION -> {
                        _destination.value = place
                        _destinationSearch.value =
                            place.displayName?.takeIf { it.isNotBlank() } ?: place.name
                        if (_source.value.position != null) {
                            _mapPointerStatus.value = MapPointerStatus.DESTINATION
                            fetchRouteFromOSRM()
                        } else {
                            _mapPointerStatus.value = MapPointerStatus.DESTINATION_WITHOUT_SOURCE
                        }
                    }

                    MapPointerStatus.DESTINATION_WITHOUT_SOURCE -> {}
                }
            }

            MapPointerStatus.SOURCE -> {
                when (_selectedTextField.value) {
                    MapPointerStatus.NONE -> {}
                    MapPointerStatus.SOURCE -> {
                        _source.value = place
                        _sourceSearch.value =
                            place.displayName?.takeIf { it.isNotBlank() } ?: place.name
                    }

                    MapPointerStatus.DESTINATION -> {
                        _destination.value = place
                        _destinationSearch.value =
                            place.displayName?.takeIf { it.isNotBlank() } ?: place.name
                    }

                    MapPointerStatus.DESTINATION_WITHOUT_SOURCE -> {}
                }
                fetchRouteFromOSRM()
                _mapPointerStatus.value = MapPointerStatus.DESTINATION
            }

            MapPointerStatus.DESTINATION_WITHOUT_SOURCE -> {
                when (_selectedTextField.value) {
                    MapPointerStatus.NONE -> {}
                    MapPointerStatus.SOURCE -> {
                        _source.value = place
                        _sourceSearch.value =
                            place.displayName?.takeIf { it.isNotBlank() } ?: place.name
                        fetchRouteFromOSRM()
                        _mapPointerStatus.value = MapPointerStatus.DESTINATION
                    }

                    MapPointerStatus.DESTINATION -> {}
                    MapPointerStatus.DESTINATION_WITHOUT_SOURCE -> {}
                }
            }

            MapPointerStatus.DESTINATION -> {
                when (_selectedTextField.value) {
                    MapPointerStatus.NONE -> {}
                    MapPointerStatus.SOURCE -> {
                        _source.value = place
                        _sourceSearch.value =
                            place.displayName?.takeIf { it.isNotBlank() } ?: place.name
                        if (_destination.value.position != null) {
                            _mapPointerStatus.value = MapPointerStatus.DESTINATION
                            fetchRouteFromOSRM()
                        } else {
                            _mapPointerStatus.value = MapPointerStatus.SOURCE
                        }
                    }

                    MapPointerStatus.DESTINATION -> {
                        _destination.value = place
                        _destinationSearch.value =
                            place.displayName?.takeIf { it.isNotBlank() } ?: place.name
                        if (_source.value.position != null) {
                            _mapPointerStatus.value = MapPointerStatus.DESTINATION
                            fetchRouteFromOSRM()
                        } else {
                            _mapPointerStatus.value = MapPointerStatus.DESTINATION_WITHOUT_SOURCE
                        }
                    }

                    MapPointerStatus.DESTINATION_WITHOUT_SOURCE -> {}
                }
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

    fun geoDecode(position: MutableStateFlow<DetailedPosition>, status: MapPointerStatus) {
        viewModelScope.launch(dispatcher) {
            val url =
                "https://nominatim.openstreetmap.org/reverse?lat=${position.value.position?.latitude}&lon=${position.value.position?.longitude}&zoom=18&format=jsonv2"
            val location: OpenStreetMap = httpClient.get(url).body()
            position.value = position.value.copy(
                name = location.name, displayName = location.display_name
            )

            when (status) {
                MapPointerStatus.NONE -> {}
                MapPointerStatus.SOURCE, MapPointerStatus.DESTINATION_WITHOUT_SOURCE -> _sourceSearch.value =
                    location.display_name?.takeIf { it.isNotBlank() } ?: location.name

                MapPointerStatus.DESTINATION -> _destinationSearch.value =
                    location.display_name?.takeIf { it.isNotBlank() } ?: location.name
            }
        }
    }

    fun geoDecode(location: String) {
        viewModelScope.launch(dispatcher) {
            val url = URLBuilder("https://nominatim.openstreetmap.org/search").apply {
                parameters.append("q", location)
                parameters.append("countrycodes", "mu")
                parameters.append("format", "jsonv2")
            }.buildString()

            val locations: List<OpenStreetMap> = httpClient.get(url).body()
            _locationResults.value = locations.map { it ->
                DetailedPosition(
                    Position(it.lon, it.lat, it.lon),
                    it.name, it.display_name
                )
            }
        }
    }

    fun updateSourceSearch(search: String) {
        _sourceSearch.value = search
        sourceSearchChannel.trySend(search)
    }

    fun updateDestinationSearch(search: String) {
        _destinationSearch.value = search
        destinationSearchChannel.trySend(search)
    }

    fun updateSelectedSearchField(status: MapPointerStatus) {
        _selectedTextField.value = status
    }

    @OptIn(FlowPreview::class)
    private fun observeSourceSearch() {
        viewModelScope.launch(dispatcher) {
            sourceSearchChannel.receiveAsFlow()
                .debounce(600)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collect { query -> geoDecode(query) }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeDestinationSearch() {
        viewModelScope.launch(dispatcher) {
            destinationSearchChannel.receiveAsFlow()
                .debounce(600)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collect { query -> geoDecode(query) }
        }
    }

}