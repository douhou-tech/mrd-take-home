package com.mrd.restaurant.discovery.core.logging

interface Logger {
    fun log(message: String)
    fun error(message: String, throwable: Throwable? = null)
}
