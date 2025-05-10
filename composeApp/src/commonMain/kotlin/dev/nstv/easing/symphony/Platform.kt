package dev.nstv.easing.symphony

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform