package com.mrd.restaurant.discovery.data.datasource

import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.remote.model.RestaurantRemoteModel
import com.mrd.restaurant.discovery.domain.usecase.FetchRestaurantByIdUseCase
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.min

interface RemoteRestaurantsDataSource {
    suspend fun getRestaurants(
        filters: RestaurantFilters = RestaurantFilters(),
        pagination: Pagination = Pagination(page = 0, pageSize = 20),
    ): PagedResult<RestaurantRemoteModel>

    fun getRestaurant(id: String): RestaurantRemoteModel?
}

internal class MockRemoteRestaurantsDataSource(
    val restaurantsJson: String,
) : RemoteRestaurantsDataSource {

    private val allRestaurants: List<RestaurantRemoteModel> by lazy {
        Json.decodeFromString(restaurantsJson)
    }

    override suspend fun getRestaurants(
        filters: RestaurantFilters,
        pagination: Pagination
    ): PagedResult<RestaurantRemoteModel> {
        var result = allRestaurants
        filters.filters.forEach { filter ->
            when (filter) {
                is RestaurantFilter.Query -> {
                    val query = filter.query.trim().lowercase()
                    if (query.isNotEmpty()) {
                        result = result.filter {
                            it.name.lowercase().contains(query) ||
                                it.cuisines.lowercase().contains(query)
                        }
                    }
                }
                is RestaurantFilter.IsOpenNow -> {
                    result = result.filter { it.isOpen }
                }
            }
        }

        val totalCount = result.size
        val pageSize = if (pagination.pageSize > 0) pagination.pageSize else totalCount
        val startIndex = max(0, pagination.page * pageSize)
        val endIndex = min(totalCount, startIndex + pageSize)

        val items = if (startIndex < totalCount) {
            result.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return PagedResult(
            items = items,
            currentPage = pagination.page,
            pageSize = pageSize,
            totalCount = totalCount
        )
    }

    override fun getRestaurant(id: String): RestaurantRemoteModel? {
        return allRestaurants.firstOrNull { it.id == id }
    }
}
