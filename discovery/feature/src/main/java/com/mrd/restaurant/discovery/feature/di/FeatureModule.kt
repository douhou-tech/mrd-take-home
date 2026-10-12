package com.mrd.restaurant.discovery.feature.di

import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.screen.browse.RestaurantDiscoveryScreen
import com.mrd.restaurant.discovery.feature.screen.browse.RestaurantDiscoveryViewModel
import com.mrd.restaurant.discovery.feature.screen.details.RestaurantDetailsScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val restaurantDiscoveryFeatureModule = module {
    viewModelOf(::RestaurantDiscoveryViewModel)
    navigation<FeatureNavKey.RestaurantDiscovery> {
        RestaurantDiscoveryScreen(
            navigator = get()
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
val restaurantDetailsFeatureModule = module {
    navigation<FeatureNavKey.RestaurantDetails> {
        RestaurantDetailsScreen()
    }
}
