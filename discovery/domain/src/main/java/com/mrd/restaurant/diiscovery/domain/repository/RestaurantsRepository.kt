package com.mrd.restaurant.diiscovery.domain.repository

import com.mrd.restaurant.diiscovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult

interface RestaurantsRepository {

    suspend fun filterRestaurants(
        filters: RestaurantFilters,
        pagination: Pagination
    ): PagedResult<Restaurant>

}

data class RestaurantFilters(
    val filters: Set<RestaurantFilter> = HashSet()
)

sealed interface RestaurantFilter {
    data class Query(val query: String) : RestaurantFilter
    data object IsOpenNow : RestaurantFilter
}