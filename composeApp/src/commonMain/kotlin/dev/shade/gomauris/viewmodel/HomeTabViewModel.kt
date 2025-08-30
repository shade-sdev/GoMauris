package dev.shade.gomauris.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.models.Result
import core.models.UiState
import dev.shade.gomauris.core.model.map.DetailedPosition
import dev.shade.gomauris.core.model.map.MapPointerStatus
import dev.shade.gomauris.core.model.map.OpenStreetMap
import dev.shade.gomauris.core.service.MapService
import dev.shade.gomauris.httpClient
import io.github.dellisd.spatialk.geojson.Position
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
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
    private val mapService: MapService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    companion object {
        const val STYLE: String = "https://tiles.openfreemap.org/styles/liberty"
    }

    private val _bottomSheetSwipeEnable = MutableStateFlow(true)
    val bottomSheetSwipeEnable: StateFlow<Boolean> = _bottomSheetSwipeEnable.asStateFlow()

    private val _source = MutableStateFlow(DetailedPosition(null, null, null))
    val source: StateFlow<DetailedPosition> = _source.asStateFlow()

    private val _destination = MutableStateFlow(DetailedPosition(null, null, null))
    val destination: StateFlow<DetailedPosition> = _destination.asStateFlow()

    private val _routeCoordinates = MutableStateFlow<List<Position>>(emptyList())
    val routeCoordinates: StateFlow<List<Position>> = _routeCoordinates.asStateFlow()

    private val _mapPointerStatus = MutableStateFlow(MapPointerStatus.NONE)

    private val _locationResults = MutableStateFlow<UiState<List<DetailedPosition>>>(UiState.Idle)
    val locationResults: StateFlow<UiState<List<DetailedPosition>>> = _locationResults.asStateFlow()

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

    fun swipeDisabled() {
        _bottomSheetSwipeEnable.value = false
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
                _locationResults.value = UiState.Idle
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
        _locationResults.value = UiState.Success(emptyList())
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
            _locationResults.value = UiState.Loading
            when (val result = mapService.searchGeoLocation(location)) {
                is Result.Error -> {
                    _locationResults.value = UiState.Error(result.message)
                }

                is Result.Success<List<DetailedPosition>> -> {
                    _locationResults.value = UiState.Success(result.data)
                }
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

    fun isLocationChosen(): Boolean {
        return this._source.value.position != null
                && this._destination.value.position != null
    }

    fun resetLocationChoice() {
        _routeCoordinates.value = emptyList()
        _source.value = DetailedPosition(null, null, null)
        _destination.value = DetailedPosition(null, null, null)
        _sourceSearch.value = null
        _destinationSearch.value = null
        _locationResults.value = UiState.Idle
        _selectedTextField.value = MapPointerStatus.NONE
        _mapPointerStatus.value = MapPointerStatus.NONE
        _bottomSheetSwipeEnable.value = true
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