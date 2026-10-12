package com.mrd.restaurant.discovery.data.datasource

import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.persistence.dao.RestaurantDao
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random

class LocalRestaurantsDataSourceImplTest {

    private val restaurantDao = mockk<RestaurantDao>(relaxed = true)
    private val subject = LocalRestaurantsDataSourceImpl(restaurantDao)

    @Test
    fun `when getRestaurants then call data source with correct arguments`() = runTest {
        val filters = RestaurantFilters()
        val pagination = Pagination(
            page = Random.nextInt(1, 10),
            pageSize = Random.nextInt(10, 50)
        )
        val offest = pagination.page * pagination.pageSize

        subject.getRestaurants(filters, pagination)

        coVerify {
            restaurantDao.getRestaurants(
                query = isNull(),
                isOpen = isNull(),
                pageSize = eq(pagination.pageSize),
                offset = eq(offest)
            )
        }
    }

    @Test
    fun `when getRestaurants is called with isOpen filter then dao should be called with isOpen = true`() = runTest {
        val filters = RestaurantFilters(
            filters = setOf(RestaurantFilter.IsOpenNow)
        )
        val pagination = Pagination(
            page = Random.nextInt(1, 10),
            pageSize = Random.nextInt(10, 50)
        )
        val offset = pagination.pageSize * pagination.page

        subject.getRestaurants(filters, pagination)


        coVerify {
            restaurantDao.getRestaurants(
                query = isNull(),
                isOpen = eq(true),
                pageSize = eq(pagination.pageSize),
                offset = eq(offset)
            )
        }
    }

    @Test
    fun `when getRestaurants called page is less than or equal to 0 then offset is 0`() = runTest {
        val filters = RestaurantFilters(
            filters = setOf(RestaurantFilter.IsOpenNow)
        )
        val pagination = Pagination(
            page = Random.nextInt(-100, 0),
            pageSize = Random.nextInt(10, 50)
        )

        subject.getRestaurants(filters, pagination)

        coVerify {
            restaurantDao.getRestaurants(
                query = isNull(),
                isOpen = eq(true),
                pageSize = eq(pagination.pageSize),
                offset = eq(0)
            )
        }
    }

    @Test
    fun `when getRestaurants pageSize is less than or equal to 0 then offset is 0`() = runTest {
        val filters = RestaurantFilters(
            filters = setOf(RestaurantFilter.IsOpenNow)
        )
        val pagination = Pagination(
            page = Random.nextInt(1, 10),
            pageSize = Random.nextInt(-100, -1)
        )

        subject.getRestaurants(filters, pagination)

        coVerify {
            restaurantDao.getRestaurants(
                query = isNull(),
                isOpen = eq(true),
                pageSize = eq(pagination.pageSize),
                offset = eq(0)
            )
        }
    }

}
