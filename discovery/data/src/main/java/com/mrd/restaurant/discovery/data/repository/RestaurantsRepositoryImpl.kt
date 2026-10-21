package com.mrd.restaurant.discovery.data.repository

import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.RemoteRestaurantsDataSource
import com.mrd.restaurant.discovery.data.mapper.RestaurantsMapper.toDomain
import com.mrd.restaurant.discovery.data.mapper.RestaurantsMapper.toEntity
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
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
        val remoteDetails = remoteDataSource.getRestaurantDetails(id)
        if (remoteDetails != null) {
            val (detailsRemote, metaRemote) = remoteDetails
            val restaurantEntity = detailsRemote.restaurantInfo.toEntity()
            localDataSource.upsertRestaurants(listOf(restaurantEntity))
            
            val menuItemsEntities = detailsRemote.menu.flatMap { category ->
                category.items.map { item ->
                    val percentageOff = if (metaRemote.onSale.itemIds.contains(item.id)) metaRemote.onSale.percentageOff else null
                    val unavailable = metaRemote.unavailable.contains(item.id)
                    com.mrd.restaurant.discovery.data.persistence.model.MenuItemEntity(
                        id = item.id,
                        restaurantId = id,
                        category = category.category,
                        name = item.name,
                        description = item.description,
                        priceInCents = item.priceInCents,
                        percentageOff = percentageOff,
                        unavailable = unavailable
                    )
                }
            }
            localDataSource.upsertMenuItems(menuItemsEntities)
        }
        return getLocalRestaurant(id)
    }

    override suspend fun getLocalRestaurant(id: String): Restaurant? {
        val localResult = localDataSource.getRestaurant(id)
        val localMenuItems = localDataSource.getMenuItemsForRestaurant(id).map { it.toDomain() }
        return localResult?.toDomain(localMenuItems)
    }

    override suspend fun setFavourite(restaurantId: String, favourite: Boolean) {
        try {
            localDataSource.setFavourite(restaurantId, favourite)
            remoteDataSource.setFavourite(restaurantId, favourite)
        } catch (ex: Exception) {
            localDataSource.setFavourite(restaurantId, !favourite)
            throw ex
        }
    }

}
