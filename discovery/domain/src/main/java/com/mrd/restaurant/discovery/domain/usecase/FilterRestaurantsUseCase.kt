package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult

class FilterRestaurantsUseCase(
    private val restaurantsRepository: RestaurantsRepository,
    private val logger: Logger
) {

    suspend operator fun invoke(
        filters: RestaurantFilters,
        pagination: Pagination
    ): EitherResult<PagedResult<Restaurant>, Error> {
        return try {
            logger.log("Filtering restaurants with filters: $filters")
            val sanitizedFilters = filters.filters.map {
                when (it) {
                    is RestaurantFilter.Query -> it.sanitize()
                    else -> it
                }
            }.toSet()
            logger.log("Sanitize restaurants filters: $sanitizedFilters")
            val result = restaurantsRepository.filterRestaurants(
                filters = RestaurantFilters(sanitizedFilters),
                pagination = pagination
            )
            logger.log("Filtered restaurants: $result")
            EitherResult.Success(result)
        } catch (e: Exception) {
            logger.error("Error filtering restaurants", e)
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

