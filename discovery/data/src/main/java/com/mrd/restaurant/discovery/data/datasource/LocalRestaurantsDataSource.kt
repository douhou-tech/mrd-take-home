package com.mrd.restaurant.discovery.data.datasource

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import kotlin.math.max

class LocalRestaurantsDataSource(
    private val restaurantDao: RestaurantDao
) {

    suspend fun getRestaurants(filters: RestaurantFilters, pagination: Pagination): PagedResult<RestaurantEntity> {
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
        val result = restaurantDao.getRestaurants(
            query = queryFilter?.query,
            isOpen = isOpen,
            pageSize = pagination.pageSize,
            offset = offset
        )
        val items = result.map { it.restaurant }
        val totalCount = result.firstOrNull()?.totalCount ?: 0
        return PagedResult(
            items = items,
            currentPage = pagination.page,
            pageSize = pagination.pageSize,
            totalCount = totalCount
        )
    }

}

@Database(entities = [RestaurantEntity::class], version = 1)
abstract class RestaurantDiscoveryDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao

}

@Entity(tableName = "Restaurant")
data class RestaurantEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val cuisines: String = "",
    val rating: Double = 0.0,
    val deliveryFeeInCents: Double = 0.0,
    val etaMinutes: Int = 0,
    val isOpen: Boolean = false,
    val imageUrl: String = ""
)

data class RestaurantEntityPagedResult(
    @Embedded val restaurant: RestaurantEntity,
    val totalCount: Int = 0
)

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
