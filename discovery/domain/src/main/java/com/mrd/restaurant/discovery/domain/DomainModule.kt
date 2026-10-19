package com.mrd.restaurant.discovery.domain

import com.mrd.restaurant.discovery.domain.usecase.FavouriteRestaurantUseCase
import com.mrd.restaurant.discovery.domain.usecase.FetchRestaurantByIdUseCase
import com.mrd.restaurant.discovery.domain.usecase.FilterRestaurantsUseCase
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
        FavouriteRestaurantUseCase(
            repository = get(),
            logger = get()
        )
    }
}
