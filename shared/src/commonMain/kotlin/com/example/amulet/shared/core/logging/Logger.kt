package com.example.amulet.shared.core.logging

import io.github.aakira.napier.Napier

object Logger {

    fun v(message: String, tag: String? = null) {
        Napier.v(message, tag = tag)
    }

    fun d(message: String, tag: String? = null) {
        Napier.d(message, tag = tag)
    }

    fun i(message: String, tag: String? = null) {
        Napier.i(message, tag = tag)
    }

    fun w(message: String, throwable: Throwable? = null, tag: String? = null) {
        Napier.w(message, throwable, tag)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String? = null) {
        Napier.e(message, throwable, tag)
    }
}
