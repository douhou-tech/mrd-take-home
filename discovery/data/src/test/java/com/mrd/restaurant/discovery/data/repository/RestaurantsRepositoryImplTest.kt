package com.mrd.restaurant.discovery.data.repository

import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.RemoteRestaurantsDataSource
import com.mrd.test.randomString
import io.mockk.coVerify
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
    fun `when getRestaurants then call remote first, persist locally, and get from local`() = runTest {
        val id = randomString(10)

        subject.getRestaurant(id)

        coVerify {
            remoteDataSource.getRestaurant(eq(id))
            localDataSource.upsertRestaurants(any())
            localDataSource.getRestaurant(eq(id))
        }
    }

}
