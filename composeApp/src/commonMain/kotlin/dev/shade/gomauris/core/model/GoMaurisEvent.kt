package dev.shade.gomauris.core.model

 sealed class GoMaurisEvent {
     data class SourceSearchChanged(val search: String): GoMaurisEvent()
}