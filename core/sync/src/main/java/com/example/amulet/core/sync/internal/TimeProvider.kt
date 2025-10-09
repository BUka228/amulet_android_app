package com.example.amulet.core.sync.internal

fun interface TimeProvider {
    fun now(): Long
}

object SystemTimeProvider : TimeProvider {
    override fun now(): Long = System.currentTimeMillis()
}
