package com.mrd.restaurant.discovery.data.repository

import com.mrd.restaurant.diiscovery.domain.model.Restaurant
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.RestaurantEntity

class RestaurantsRepositoryImpl(
    private val localDataSource: LocalRestaurantsDataSource
) : RestaurantsRepository {

    override suspend fun filterRestaurants(
        filters: RestaurantFilters,
        pagination: Pagination
    ): PagedResult<Restaurant> {
        val result = localDataSource.getRestaurants(filters, pagination)
        val domainItems = result.items.map { it.toDomain() }
        return PagedResult(
            items = domainItems,
            pageSize = result.pageSize,
            currentPage = result.currentPage,
            totalCount = result.totalCount
        )
    }

}

private fun RestaurantEntity.toDomain(): Restaurant {
    return Restaurant(
        id = id,
        name = name,
        cuisines = cuisines,
        rating = rating,
        deliveryFeeInCents = deliveryFeeInCents,
        etaMinutes = etaMinutes,
        isOpen = isOpen,
        imageUrl = imageUrl
    )
}