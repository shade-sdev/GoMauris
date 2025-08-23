package dev.shade.gomauris

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform