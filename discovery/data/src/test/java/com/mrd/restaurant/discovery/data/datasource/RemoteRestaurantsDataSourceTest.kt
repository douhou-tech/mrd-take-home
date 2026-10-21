package com.mrd.restaurant.discovery.data.datasource

import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.test.shouldContainAll
import com.mrd.test.shouldContainAny
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldEqual
import io.kotest.matchers.equals.shouldNotEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RemoteRestaurantsDataSourceTest {

    private val subject: RemoteRestaurantsDataSource = MockRemoteRestaurantsDataSource(
        restaurantsJson = loadRestaurantsJson(),
        restaurantDetailsJson = loadRestaurantDetailsJson(),
        restaurantDetailsMetaJson = loadRestaurantDetailsMetaJson()
    )

    @Test
    fun `when getRestaurants without filters then return 100 Cape Town CBD restaurants paginated`() = runTest {
        val result = subject.getRestaurants()

        with(result) {
            items.size shouldBeEqual 20
            totalCount shouldBeEqual 100
            items.first().id shouldBeEqual "cpt-cbd-001"
            items shouldContainAny {
                it.name.startsWith("Eastern Food Bazaar")
            }
            items shouldContainAny {
                it.name.startsWith("Kloof Street House")
            }
        }
    }

    @Test
    fun `when getRestaurants requesting 100 pageSize then return all 100 items`() = runTest {
        val pagination = Pagination(page = 0, pageSize = 100)
        val result = subject.getRestaurants(pagination = pagination)

        with (result) {
            totalCount shouldBeEqual 100
            items.size shouldBeEqual 100
        }
    }

    @Test
    fun `when getRestaurants with query filter then return matching restaurants`() = runTest {
        val filters = RestaurantFilters(
            filters = setOf(RestaurantFilter.Query("chocolate"))
        )

        val result = subject.getRestaurants(filters = filters, pagination = Pagination(page = 0, pageSize = 100))

        with(result) {
            items.size shouldBeGreaterThan 0
            items shouldContainAll { item ->
                item.name.contains("Chocolate", ignoreCase = true)
            }
        }
    }

    @Test
    fun `when getRestaurants with isOpenNow filter then return only open restaurants`() = runTest {
        val filters = RestaurantFilters(
            filters = setOf(RestaurantFilter.IsOpenNow)
        )

        val result = subject.getRestaurants(filters = filters, pagination = Pagination(page = 0, pageSize = 100))

        with(result) {
            items.size shouldBeGreaterThan 0
            items shouldContainAll { item -> item.isOpen }
        }
    }

    @Test
    fun `when getRestaurants with pagination then return correct paginated sublist`() = runTest {
        val page0 = subject.getRestaurants(pagination = Pagination(page = 0, pageSize = 10))
        val page1 = subject.getRestaurants(pagination = Pagination(page = 1, pageSize = 10))

        with(page0) {
            items.size shouldBeEqual 10
            totalCount shouldBeEqual 100
            items.first().id shouldBeEqual "cpt-cbd-001"
        }
        with(page1) {
            items.size shouldBeEqual 10
            totalCount shouldBeEqual 100
            items.first().id shouldBeEqual "cpt-cbd-011"
        }
    }

    @Test
    fun `when getRestaurant with invalid id then return null`() = runTest {
        val result = subject.getRestaurant("invalid-id")

        result shouldBe null
    }

    @Test
    fun `when getRestaurant with valid id then return correct item is returned`() = runTest {
        val id = "cpt-cbd-001"

        val result = subject.getRestaurant(id)

        result shouldNotEqual null
        result?.id shouldEqual id
    }

    @Test
    fun `when getRestaurantDetails with valid id then return correct details and meta`() = runTest {
        val id = "cpt-cbd-001"

        val result = subject.getRestaurantDetails(id)

        result shouldNotEqual null
        result?.first?.id shouldEqual id
        result?.second shouldNotEqual null
    }

    @Test
    fun `when getRestaurantDetails with invalid id then return null`() = runTest {
        val id = "invalid-id"

        val result = subject.getRestaurantDetails(id)

        result shouldBe null
    }

}

private fun loadRestaurantsJson() = readResourceFileText("mocks/restaurants.json")
private fun loadRestaurantDetailsJson() = readResourceFileText("mocks/restaurant_details.json")
private fun loadRestaurantDetailsMetaJson() = readResourceFileText("mocks/restaurant_details_meta.json")

private fun readResourceFileText(
    path: String,
    default: String = "{}"
): String {
    return try {
        val file = Thread.currentThread().contextClassLoader?.getResource(path)
            ?: RemoteRestaurantsDataSourceTest::class.java.classLoader?.getResource(path)
        file?.readText() ?: default
    } catch (_: Exception) {
        default
    }
}
