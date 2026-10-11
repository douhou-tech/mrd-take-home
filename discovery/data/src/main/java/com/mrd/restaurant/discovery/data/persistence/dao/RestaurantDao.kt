package com.mrd.restaurant.discovery.data.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import com.mrd.restaurant.discovery.data.persistence.model.RestaurantEntityPagedResult

@Dao
interface RestaurantDao {

    @Query(
        """
        SELECT *, COUNT(*) OVER() AS totalCount 
        FROM Restaurant 
        WHERE 
            (:query IS NULL OR name LIKE '%' || :query || '%') AND 
            (:isOpen IS NULL OR isOpen = :isOpen)
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

}
