package com.mrd.restaurant.discovery.demo.logging

import com.mrd.restaurant.discovery.core.logging.Logger
import timber.log.Timber

class ConsoleLogger : Logger {

    override fun log(message: String) {
        Timber.tag(TAG).i(message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Timber.tag(TAG).e(throwable, message)
    }

    companion object {
        const val TAG = "ConsoleLogger"
    }
}