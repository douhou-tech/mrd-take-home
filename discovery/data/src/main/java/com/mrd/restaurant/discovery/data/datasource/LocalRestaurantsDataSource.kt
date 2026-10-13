package com.mrd.restaurant.discovery.data.datasource

import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.persistence.dao.RestaurantDao
import com.mrd.restaurant.discovery.data.persistence.model.RestaurantEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

interface LocalRestaurantsDataSource {
    suspend fun getRestaurants(filters: RestaurantFilters, pagination: Pagination): PagedResult<RestaurantEntity>
    suspend fun upsertRestaurants(restaurants: List<RestaurantEntity>)
    suspend fun getRestaurant(id: String): RestaurantEntity
}

class LocalRestaurantsDataSourceImpl(
    private val restaurantDao: RestaurantDao
): LocalRestaurantsDataSource {

    override suspend fun getRestaurants(filters: RestaurantFilters, pagination: Pagination): PagedResult<RestaurantEntity> {
        val filters = filters.filters
        val queryFilter = filters.firstOrNull {
            it is RestaurantFilter.Query
        } as? RestaurantFilter.Query
        val isOpenFilter = filters.firstOrNull {
            it is RestaurantFilter.IsOpenNow
        } as? RestaurantFilter.IsOpenNow
        val isOpen = when (isOpenFilter) {
            null -> null
            else -> true
        }
        val offset = max(0, pagination.page * pagination.pageSize)
        return withContext(Dispatchers.IO) {
            val result = restaurantDao.getRestaurants(
                query = queryFilter?.query,
                isOpen = isOpen,
                pageSize = pagination.pageSize,
                offset = offset
            )
            val items = result.map { it.restaurant }
            val totalCount = result.firstOrNull()?.totalCount ?: 0
            PagedResult(
                items = items,
                currentPage = pagination.page,
                pageSize = pagination.pageSize,
                totalCount = totalCount
            )
        }
    }

    override suspend fun upsertRestaurants(restaurants: List<RestaurantEntity>) {
        withContext(Dispatchers.IO) {
            restaurantDao.upsertRestaurants(restaurants)
        }
    }

    override suspend fun getRestaurant(id: String): RestaurantEntity {
        return withContext(Dispatchers.IO) {
            restaurantDao.getRestaurant(id)
        }
    }

}
