package com.mrd.restaurant.diiscovery.domain.usecase

import com.mrd.restaurant.diiscovery.domain.model.Restaurant
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.core.primitive.model.PaginationResult

class FilterRestaurantsUseCase(
    private val restaurantsRepository: RestaurantsRepository
) {

    suspend operator fun invoke(
        filters: RestaurantFilters,
        pagination: Pagination
    ): EitherResult<PaginationResult<Restaurant>, Error> {
        return try {
            val result = restaurantsRepository.filterRestaurants(filters, pagination)
            EitherResult.Success(result)
        } catch (_: Exception) {
            EitherResult.Error(Error.Unknown)
        }
    }

    sealed interface Error {
        data object Unknown : Error
    }
}

