package com.mrd.restaurant.discovery.data.repository

import com.mrd.restaurant.diiscovery.domain.model.Restaurant
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.mapper.RestaurantsMapper.toDomain

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

