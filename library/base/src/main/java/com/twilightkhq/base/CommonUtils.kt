package com.twilightkhq.base

fun Int.seconds(): Long {
    return this * 1000L
}

fun Int.minutes(): Long {
    return this * 60 * 1000L
}