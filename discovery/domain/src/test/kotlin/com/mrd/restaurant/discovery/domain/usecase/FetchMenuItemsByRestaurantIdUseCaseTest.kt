package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.model.MenuItem
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.test.randomString
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FetchMenuItemsByRestaurantIdUseCaseTest {

    private val repository = mockk<RestaurantsRepository>()
    private val logger = mockk<Logger>(relaxed = true)

    private val subject = FetchMenuItemsByRestaurantIdUseCase(repository, logger)

    @Test
    fun `when invoke then should call repository`() = runTest {
        val restaurantId = randomString(10)
        val mockRestaurant = mockk<com.mrd.restaurant.discovery.domain.model.Restaurant>(relaxed = true)
        coEvery { repository.getRestaurant(any()) } returns mockRestaurant

        subject.invoke(restaurantId)

        coVerify {
            repository.getRestaurant(eq(restaurantId))
        }
    }

    @Test
    fun `when invoke and repository returns restaurant then success result should be returned`() = runTest {
        val restaurantId = randomString(10)
        val expected = listOf(mockk<MenuItem>())
        val mockRestaurant = mockk<com.mrd.restaurant.discovery.domain.model.Restaurant> {
            io.mockk.every { menuItems } returns expected
        }
        coEvery { repository.getRestaurant(any()) } returns mockRestaurant

        val actual = subject.invoke(restaurantId)

        actual shouldBeEqual EitherResult.Success(expected)
    }

    @Test
    fun `when invoke and repository throws an error then unknown error result should be returned`() = runTest {
        val restaurantId = randomString(10)
        coEvery { repository.getRestaurant(any()) } throws Exception("Test exception")

        val actual = subject.invoke(restaurantId)

        actual shouldBeEqual EitherResult.Error(FetchMenuItemsByRestaurantIdUseCase.Error.Unknown)
    }

}
