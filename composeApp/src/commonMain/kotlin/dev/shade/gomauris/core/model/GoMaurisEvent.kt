package dev.shade.gomauris.core.model

 sealed class GoMaurisEvent {
     data class SourceSearchChanged(val search: String): GoMaurisEvent()
     data class DestinationSearchChanged(val search: String): GoMaurisEvent()
 }