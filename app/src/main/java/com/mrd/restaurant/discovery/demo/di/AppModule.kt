package com.mrd.restaurant.discovery.demo.di

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.demo.logging.ConsoleLogger
import com.mrd.restaurant.discovery.demo.navigation.AppNavigator
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import org.koin.dsl.module

val appModule = module {
   single<Logger> { ConsoleLogger() }
   single {
      AppNavigator(startDestination = FeatureNavKey.RestaurantDiscovery)
   }
   single<FeatureNavigator> {
      get<AppNavigator>()
   }
}