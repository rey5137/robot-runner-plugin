package com.github.rey5137.robotrunnerplugin.http

object Versions {
    private val V1_0 = byteArrayOf(1, 0)
    private val V1_1 = byteArrayOf(1, 1)
    val CURRENT = V1_1
    val current: ByteArray
        get() = CURRENT.clone()
}