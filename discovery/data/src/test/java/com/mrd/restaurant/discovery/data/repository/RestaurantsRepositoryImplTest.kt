package com.mrd.restaurant.discovery.data.repository

import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.RemoteRestaurantsDataSource
import com.mrd.test.randomString
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random

class RestaurantsRepositoryImplTest {

    private val localDataSource = mockk<LocalRestaurantsDataSource>(relaxed = true)
    private val remoteDataSource = mockk<RemoteRestaurantsDataSource>(relaxed = true)
    private val subject = RestaurantsRepositoryImpl(localDataSource, remoteDataSource)

    @Test
    fun `when filterRestaurants then call remote first, persist locally, and get from local`() = runTest {
        val filters = RestaurantFilters()
        val pagination = Pagination(
            pageSize = Random.nextInt(10, 50),
            page = Random.nextInt(1, 100)
        )

        subject.filterRestaurants(filters, pagination)

        coVerify {
            remoteDataSource.getRestaurants(eq(filters), eq(pagination))
            localDataSource.upsertRestaurants(any())
            localDataSource.getRestaurants(eq(filters), eq(pagination))
        }
    }

    @Test
    fun `when getRestaurant then call remote first, persist locally, and get from local`() = runTest {
        val id = randomString(10)
        
        val details = mockk<com.mrd.restaurant.discovery.data.remote.model.RestaurantDetailsRemoteModel>(relaxed = true)
        val meta = mockk<com.mrd.restaurant.discovery.data.remote.model.RestaurantDetailsMetaRemoteModel>(relaxed = true)
        
        io.mockk.coEvery { remoteDataSource.getRestaurantDetails(any()) } returns Pair(details, meta)

        subject.getRestaurant(id)

        coVerify {
            remoteDataSource.getRestaurantDetails(eq(id))
            localDataSource.upsertRestaurants(any())
            localDataSource.upsertMenuItems(any())
            localDataSource.getRestaurant(eq(id))
            localDataSource.getMenuItemsForRestaurant(eq(id))
        }
    }

    @Test
    fun `when setFavourite then call both local and remote data sources in correct order`() = runTest {
        val id = randomString(10)
        val favourite = true

        subject.setFavourite(id, favourite)

        coVerifyOrder {
            localDataSource.setFavourite(eq(id), eq(favourite))
            remoteDataSource.setFavourite(eq(id), eq(favourite))
        }
    }

}
