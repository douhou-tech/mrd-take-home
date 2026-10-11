package com.mrd.restaurant.discovery.demo.di

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.demo.logging.ConsoleLogger
import org.koin.dsl.module

val appModule = module {
   single<Logger> { ConsoleLogger() }
}