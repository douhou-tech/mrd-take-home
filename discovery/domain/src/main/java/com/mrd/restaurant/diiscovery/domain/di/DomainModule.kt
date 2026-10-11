package com.mrd.restaurant.diiscovery.domain.di

import com.mrd.restaurant.diiscovery.domain.usecase.FilterRestaurantsUseCase
import org.koin.dsl.module

val restaurantDiscoveryDomainModule = module {
    factory { FilterRestaurantsUseCase(restaurantsRepository = get()) }
}
