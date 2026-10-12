package com.mrd.restaurant.discovery.domain

import com.mrd.restaurant.discovery.domain.usecase.FilterRestaurantsUseCase
import org.koin.dsl.module

val restaurantDiscoveryDomainModule = module {
    factory { FilterRestaurantsUseCase(
        restaurantsRepository = get(),
        logger = get()
    ) }
}
