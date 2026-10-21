package com.mrd.restaurant.discovery.data.datasource

import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.persistence.dao.RestaurantDao
import com.mrd.restaurant.discovery.data.persistence.model.RestaurantEntity
import com.mrd.restaurant.discovery.data.persistence.model.MenuItemEntity
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.max

interface LocalRestaurantsDataSource {
    fun observeFavouriteIds(): Flow<Set<String>>
    suspend fun getRestaurants(filters: RestaurantFilters, pagination: Pagination): PagedResult<RestaurantEntity>
    suspend fun upsertRestaurants(restaurants: List<RestaurantEntity>)
    suspend fun upsertMenuItems(menuItems: List<MenuItemEntity>)
    suspend fun getRestaurant(id: String): RestaurantEntity?
    suspend fun getMenuItemsForRestaurant(id: String): List<MenuItemEntity>
    suspend fun setFavourite(restaurantId: String, favourite: Boolean)
}

class LocalRestaurantsDataSourceImpl(
    private val restaurantDao: RestaurantDao
): LocalRestaurantsDataSource {

    override fun observeFavouriteIds(): Flow<Set<String>> {
        return restaurantDao.observeFavouriteIds().map { it.toSet() }
    }

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
            val ids = restaurants.map { it.id }
            val existing = restaurantDao.getRestaurantsByIds(ids).associateBy { it.id }
            val merged = restaurants.map { entity ->
                val existingFavourite = existing[entity.id]?.favourite ?: false
                entity.copy(favourite = entity.favourite || existingFavourite)
            }
            restaurantDao.upsertRestaurants(merged)
        }
    }

    override suspend fun upsertMenuItems(menuItems: List<MenuItemEntity>) {
        withContext(Dispatchers.IO) {
            restaurantDao.upsertMenuItems(menuItems)
        }
    }

    override suspend fun getRestaurant(id: String): RestaurantEntity {
        return withContext(Dispatchers.IO) {
            restaurantDao.getRestaurant(id)
        }
    }

    override suspend fun getMenuItemsForRestaurant(id: String): List<MenuItemEntity> {
        return withContext(Dispatchers.IO) {
            restaurantDao.getMenuItemsForRestaurant(id)
        }
    }

    override suspend fun setFavourite(restaurantId: String, favourite: Boolean) {
        withContext(Dispatchers.IO) {
            restaurantDao.updateFavourite(restaurantId, favourite)
        }
    }

}
