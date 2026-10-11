package com.mrd.restaurant.discovery.data.datasource

import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteDataSourceTest {

    private val subject: RemoteDataSource = MockRemoteDataSource(loadRestaurantsJson())

    @Test
    fun `when getRestaurants without filters then return 100 Cape Town CBD restaurants paginated`() = runTest {
        val result = subject.getRestaurants()

        assertEquals(100, result.totalCount)
        assertEquals(20, result.items.size)
        assertTrue(result.items.any { it.name.startsWith("Eastern Food Bazaar") })
        assertTrue(result.items.any { it.name.startsWith("Kloof Street House") })
    }

    @Test
    fun `when getRestaurants requesting 100 pageSize then return all 100 items`() = runTest {
        val pagination = Pagination(page = 0, pageSize = 100)
        val result = subject.getRestaurants(pagination = pagination)

        assertEquals(100, result.totalCount)
        assertEquals(100, result.items.size)
    }

    @Test
    fun `when getRestaurants with query filter then return matching restaurants`() = runTest {
        val filters = RestaurantFilters(
            filters = setOf(RestaurantFilter.Query("chocolate"))
        )

        val result = subject.getRestaurants(filters = filters, pagination = Pagination(page = 0, pageSize = 100))

        assertTrue(result.items.isNotEmpty())
        assertTrue(result.items.all { it.name.contains("Chocolate", ignoreCase = true) || it.cuisines.contains("Chocolate", ignoreCase = true) })
    }

    @Test
    fun `when getRestaurants with isOpenNow filter then return only open restaurants`() = runTest {
        val filters = RestaurantFilters(
            filters = setOf(RestaurantFilter.IsOpenNow)
        )

        val result = subject.getRestaurants(filters = filters, pagination = Pagination(page = 0, pageSize = 100))

        assertTrue(result.items.isNotEmpty())
        assertTrue(result.items.all { it.isOpen })
    }

    @Test
    fun `when getRestaurants with pagination then return correct paginated sublist`() = runTest {
        val page0 = subject.getRestaurants(pagination = Pagination(page = 0, pageSize = 10))
        val page1 = subject.getRestaurants(pagination = Pagination(page = 1, pageSize = 10))

        assertEquals(10, page0.items.size)
        assertEquals(10, page1.items.size)
        assertEquals(100, page0.totalCount)
        assertEquals("cpt-cbd-001", page0.items.first().id)
        assertEquals("cpt-cbd-011", page1.items.first().id)
    }
}

private fun loadRestaurantsJson(): String {
    return try {
        Thread.currentThread().contextClassLoader?.getResource("mocks/restaurants.json")?.readText()
            ?: RemoteDataSourceTest::class.java.classLoader?.getResource("mocks/restaurants.json")?.readText()
            ?: "[]"
    } catch (_: Exception) {
        "[]"
    }
}
