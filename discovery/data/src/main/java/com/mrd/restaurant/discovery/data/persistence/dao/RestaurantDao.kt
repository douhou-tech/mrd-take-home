package com.mrd.restaurant.discovery.data.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mrd.restaurant.discovery.data.persistence.model.MenuItemEntity
import com.mrd.restaurant.discovery.data.persistence.model.RestaurantEntity
import com.mrd.restaurant.discovery.data.persistence.model.RestaurantEntityPagedResult
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    @Upsert
    suspend fun upsertRestaurants(restaurants: List<RestaurantEntity>)

    @Upsert
    suspend fun upsertMenuItems(menuItems: List<MenuItemEntity>)

    @Query(
        """
        SELECT *, COUNT(*) OVER() AS totalCount 
        FROM Restaurant 
        WHERE 
            (:query IS NULL OR name LIKE '%' || :query || '%') AND 
            (:isOpen IS NULL OR isOpen = :isOpen)
        ORDER BY isOpen DESC, name
        LIMIT :pageSize
        OFFSET :offset
        """
    )
    suspend fun getRestaurants(
        query: String? = null,
        isOpen: Boolean? = null,
        pageSize: Int,
        offset: Int
    ): List<RestaurantEntityPagedResult>

    @Query("SELECT * FROM Restaurant WHERE id = :id LIMIT 1")
    fun getRestaurant(id: String): RestaurantEntity

    @Query("SELECT * FROM Restaurant WHERE id IN (:ids)")
    suspend fun getRestaurantsByIds(ids: List<String>): List<RestaurantEntity>

    @Query("UPDATE Restaurant SET favourite = :favourite WHERE id = :id")
    suspend fun updateFavourite(id: String, favourite: Boolean)

    @Query("SELECT id FROM Restaurant WHERE favourite = 1")
    fun observeFavouriteIds(): Flow<List<String>>

    @Query("SELECT * FROM MenuItem WHERE restaurantId = :restaurantId")
    suspend fun getMenuItemsForRestaurant(restaurantId: String): List<MenuItemEntity>
}
