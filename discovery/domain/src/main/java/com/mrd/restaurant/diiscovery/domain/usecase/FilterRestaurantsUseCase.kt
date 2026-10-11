package com.mrd.restaurant.diiscovery.domain.usecase

import com.mrd.restaurant.diiscovery.domain.model.Restaurant
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult

class FilterRestaurantsUseCase(
    private val restaurantsRepository: RestaurantsRepository
) {

    suspend operator fun invoke(
        filters: RestaurantFilters,
        pagination: Pagination
    ): EitherResult<PagedResult<Restaurant>, Error> {
        return try {
            val sanitizedFilters = filters.filters.map {
                when (it) {
                    is RestaurantFilter.Query -> it.sanitize()
                    else -> it
                }
            }.toSet()
            val result = restaurantsRepository.filterRestaurants(
                filters = RestaurantFilters(sanitizedFilters),
                pagination = pagination
            )
            EitherResult.Success(result)
        } catch (_: Exception) {
            EitherResult.Error(Error.Unknown)
        }
    }

    private fun RestaurantFilter.Query.sanitize(): RestaurantFilter.Query {
        val query = this.query
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
        return RestaurantFilter.Query(query)
    }

    sealed interface Error {
        data object Unknown : Error
    }
}

