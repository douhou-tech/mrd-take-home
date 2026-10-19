package com.mrd.restaurant.discovery.demo.di

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.demo.logging.ConsoleLogger
import com.mrd.restaurant.discovery.demo.navigation.AppNavigator
import com.mrd.restaurant.discovery.feature.di.FeatureModuleQualifiers
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
   single<Logger> { ConsoleLogger() }
   single<AppNavigator> {
      AppNavigator(
         startDestination = FeatureNavKey.RestaurantDiscovery,
         deepLinkMatchers = listOf(get(named(FeatureModuleQualifiers.RESTAURANT_DETAILS_DEEPLINK_MATCHER))),
         logger = get()
      )
   }
   single<FeatureNavigator> {
      get<AppNavigator>()
   }
}