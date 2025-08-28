package dev.shade.gomauris.core.service

import core.models.Result
import dev.shade.gomauris.core.model.map.DetailedPosition
import dev.shade.gomauris.core.model.map.OpenStreetMap
import dev.shade.gomauris.httpClient
import io.github.dellisd.spatialk.geojson.Position
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder

class MapService {

    suspend fun searchGeoLocation(location: String): Result<List<DetailedPosition>> {
        val url = URLBuilder("https://nominatim.openstreetmap.org/search").apply {
            parameters.append("q", location)
            parameters.append("countrycodes", "mu")
            parameters.append("format", "jsonv2")
        }.buildString()

        try {
            val locations: List<OpenStreetMap> = httpClient.get(url).body()
            val detailedPositions = locations.map { it ->
                DetailedPosition(
                    Position(it.lon, it.lat, it.lon),
                    it.name, it.display_name
                )
            }
            return Result.Success(detailedPositions)
        } catch (e: Exception) {
            return Result.Error(e.message)
        }
    }

}