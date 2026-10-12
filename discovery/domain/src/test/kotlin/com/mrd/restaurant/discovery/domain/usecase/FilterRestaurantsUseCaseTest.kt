package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.KoinTest

class FilterRestaurantsUseCaseTest : KoinTest {

    private val restaurantsRepository = mockk<RestaurantsRepository>()
    private val logger = mockk<Logger>(relaxed = true)
    private val subject = FilterRestaurantsUseCase(restaurantsRepository, logger)

    @Test
    fun `when invoke then call repository should be called with correct parameters`() = runTest {
        val filters = RestaurantFilters()
        val pagination = mockk<Pagination>()

        subject(filters, pagination)

        coVerify { restaurantsRepository.filterRestaurants(eq(filters), eq(pagination)) }
    }

    @Test
    fun `when invoke with query filter then the query string should be sanitized`() = runTest {
        val filters = RestaurantFilters(
            filters = setOf(RestaurantFilter.Query(" hEllO   Filter   "))
        )
        val filtersSlot = slot<RestaurantFilters>()
        val pagination = mockk<Pagination>()
        coEvery { restaurantsRepository.filterRestaurants(capture(filtersSlot), any()) }

        subject(filters, pagination)

        val actual = filtersSlot.captured.filters.first()
        actual shouldBeEqual RestaurantFilter.Query("hello filter")
    }

    @Test
    fun `when invoke and success then success result should be returned`() = runTest {
        val filters = RestaurantFilters()
        val pagination = mockk<Pagination>()
        val data = mockk<PagedResult<Restaurant>>()
        coEvery { restaurantsRepository.filterRestaurants(eq(filters), eq(pagination)) } returns data

        val actual = subject(filters, pagination)

        actual shouldBeEqual EitherResult.Success(data)
    }

    @Test
    fun `when invoke and exception is thrown then unknown error result should be returned`() = runTest {
        val filters = RestaurantFilters()
        val pagination = mockk<Pagination>()
        coEvery {
            restaurantsRepository.filterRestaurants(eq(filters), eq(pagination))
        } throws mockk<Exception>()

        val actual = subject(filters, pagination)

        actual shouldBeEqual EitherResult.Error(FilterRestaurantsUseCase.Error.Unknown)
    }
}
