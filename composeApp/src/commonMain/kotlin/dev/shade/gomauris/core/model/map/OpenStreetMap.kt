package dev.shade.gomauris.core.model.map

import kotlinx.serialization.Serializable

@Serializable
data class OpenStreetMap(val lat: Double,
                         val lon: Double,
                         val name: String?,
                         val display_name: String?)