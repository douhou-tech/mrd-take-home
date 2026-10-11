package com.mrd.restaurant.discovery.data.repository

import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random

class RestaurantsRepositoryImplTest {

    private val localDataSource = mockk<LocalRestaurantsDataSource>(relaxed = true)
    private val subject = RestaurantsRepositoryImpl(localDataSource)

    @Test
    fun `when filterRestaurants then call data source with correct arguments`() = runTest {
        val filters = RestaurantFilters()
        val pagination = Pagination(
            pageSize = Random.nextInt(10, 50),
            page = Random.nextInt(1, 100)
        )

        subject.filterRestaurants(filters, pagination)

        coVerify {
            localDataSource.getRestaurants(eq(filters), eq(pagination))
        }
    }

}