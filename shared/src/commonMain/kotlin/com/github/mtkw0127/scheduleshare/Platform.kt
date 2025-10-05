package com.github.mtkw0127.scheduleshare

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform