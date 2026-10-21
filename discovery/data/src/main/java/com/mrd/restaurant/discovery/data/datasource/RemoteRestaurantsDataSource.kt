package com.mrd.restaurant.discovery.data.datasource

import androidx.annotation.VisibleForTesting
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.remote.model.RestaurantDetailsMetaRemoteModel
import com.mrd.restaurant.discovery.data.remote.model.RestaurantDetailsRemoteModel
import com.mrd.restaurant.discovery.data.remote.model.RestaurantRemoteModel
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

interface RemoteRestaurantsDataSource {
    suspend fun getRestaurants(
        filters: RestaurantFilters = RestaurantFilters(),
        pagination: Pagination = Pagination(page = 0, pageSize = 20),
    ): PagedResult<RestaurantRemoteModel>

    suspend fun getRestaurant(id: String): RestaurantRemoteModel?

    suspend fun getRestaurantDetails(id: String): RestaurantDetailsRemoteModel?

    suspend fun setFavourite(restaurantId: String, favourite: Boolean)
}

// TODO replace with real API implementation
internal class MockRemoteRestaurantsDataSource(
    val restaurantsJson: String,
    val restaurantDetailsJson: String = "[]",
    val restaurantDetailsMetaJson: String = "{}"
) : RemoteRestaurantsDataSource {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    private val allRestaurants: List<RestaurantRemoteModel> by lazy {
        json.decodeFromString(restaurantsJson)
    }

    private val allRestaurantDetails: List<RestaurantDetailsRemoteModel> by lazy {
        json.decodeFromString(restaurantDetailsJson)
    }

    private val allRestaurantDetailsMeta: Map<String, RestaurantDetailsMetaRemoteModel> by lazy {
        json.decodeFromString(restaurantDetailsMetaJson)
    }

    private val favouriteMap = mutableMapOf<String, Boolean>()

    override suspend fun getRestaurants(
        filters: RestaurantFilters,
        pagination: Pagination
    ): PagedResult<RestaurantRemoteModel> {
        delay(1200L.milliseconds)
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

    override suspend fun getRestaurant(id: String): RestaurantRemoteModel? {
        networkDelay()
        tryFakeException()
        return allRestaurants.firstOrNull { it.id == id }
    }

    override suspend fun getRestaurantDetails(id: String): RestaurantDetailsRemoteModel? {
        networkDelay()
        tryFakeException()
        val details = allRestaurantDetails.firstOrNull { it.id == id } ?: return null
//        val meta = allRestaurantDetailsMeta[id] ?: return null
        return details
    }

    override suspend fun setFavourite(restaurantId: String, favourite: Boolean) {
        favouriteMap[restaurantId] = favourite
    }

    @VisibleForTesting
    suspend fun networkDelay() {
        val delay = Random.nextLong(300L, 1200L)
        delay(delay.milliseconds)
    }

    @VisibleForTesting
    fun tryFakeException() {
        val chance = Random.nextInt()
        if (chance % 5 == 0) {
            throw Exception("Fake exception")
        }
    }
}
