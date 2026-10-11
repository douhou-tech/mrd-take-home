package com.mrd.restaurant.discovery.demo.logging

import com.mrd.restaurant.discovery.core.logging.Logger
import timber.log.Timber

class ConsoleLogger : Logger {

    override fun log(message: String) {
        Timber.d(message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Timber.e(throwable, message)
    }
}