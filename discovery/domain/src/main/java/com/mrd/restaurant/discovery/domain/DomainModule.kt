package com.mrd.restaurant.discovery.domain

import com.mrd.restaurant.discovery.domain.usecase.FetchRestaurantByIdUseCase
import com.mrd.restaurant.discovery.domain.usecase.FilterRestaurantsUseCase
import com.mrd.restaurant.discovery.domain.usecase.WishlistRestaurantUseCase
import org.koin.dsl.module

val restaurantDiscoveryDomainModule = module {
    factory {
        FilterRestaurantsUseCase(
            repository = get(),
            logger = get()
        )
    }
    factory {
        FetchRestaurantByIdUseCase(
            repository = get(),
            logger = get()
        )
    }
    factory {
        WishlistRestaurantUseCase(
            repository = get(),
            logger = get()
        )
    }
}
