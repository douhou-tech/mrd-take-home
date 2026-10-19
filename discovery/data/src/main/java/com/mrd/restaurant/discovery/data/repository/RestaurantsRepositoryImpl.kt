package com.mrd.restaurant.discovery.data.repository

import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.RemoteRestaurantsDataSource
import com.mrd.restaurant.discovery.data.mapper.RestaurantsMapper.toDomain
import com.mrd.restaurant.discovery.data.mapper.RestaurantsMapper.toEntity
import kotlinx.coroutines.flow.Flow

class RestaurantsRepositoryImpl(
    private val localDataSource: LocalRestaurantsDataSource,
    private val remoteDataSource: RemoteRestaurantsDataSource
) : RestaurantsRepository {

    override fun observeFavouriteIds(): Flow<Set<String>> {
        return localDataSource.observeFavouriteIds()
    }

    override suspend fun filterRestaurants(
        filters: RestaurantFilters,
        pagination: Pagination
    ): PagedResult<Restaurant> {
        val remoteResult = remoteDataSource.getRestaurants(filters, pagination)
        val entities = remoteResult.items.map { it.toEntity() }
        localDataSource.upsertRestaurants(entities)
        val localResult = localDataSource.getRestaurants(filters, pagination)
        val domainItems = localResult.items.map { it.toDomain() }

        return PagedResult(
            items = domainItems,
            pageSize = localResult.pageSize,
            currentPage = localResult.currentPage,
            totalCount = remoteResult.totalCount
        )
    }

    override suspend fun getRestaurant(id: String): Restaurant? {
        val remoteResult = remoteDataSource.getRestaurant(id)
        return remoteResult?.toEntity()?.let { entity ->
            localDataSource.upsertRestaurants(listOf(entity))
            val localResult  = localDataSource.getRestaurant(id)
            localResult.toDomain()
        }
    }

    override suspend fun setFavourite(restaurantId: String, favourite: Boolean) {
        remoteDataSource.setFavourite(restaurantId, favourite)
        localDataSource.setFavourite(restaurantId, favourite)
    }

}
