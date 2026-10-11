package com.mrd.restaurant.discovery.feature.di

import com.mrd.restaurant.discovery.feature.ui.RestaurantDiscoveryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val restaurantDiscoveryFeatureModule = module {
    viewModelOf(::RestaurantDiscoveryViewModel)
}
