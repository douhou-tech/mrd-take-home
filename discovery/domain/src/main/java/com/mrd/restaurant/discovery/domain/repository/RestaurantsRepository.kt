package com.mrd.restaurant.discovery.domain.repository

import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import kotlinx.coroutines.flow.Flow

interface RestaurantsRepository {

    fun observeFavouriteIds(): Flow<Set<String>>

    suspend fun filterRestaurants(
        filters: RestaurantFilters,
        pagination: Pagination
    ): PagedResult<Restaurant>

    suspend fun getRestaurant(
        id: String
    ): Restaurant?

    suspend fun getLocalRestaurant(
        id: String
    ): Restaurant?

    suspend fun setFavourite(restaurantId: String, favourite: Boolean)
}

data class RestaurantFilters(
    val filters: Set<RestaurantFilter> = HashSet()
)

sealed interface RestaurantFilter {
    data class Query(val query: String) : RestaurantFilter
    data object IsOpenNow : RestaurantFilter
}
