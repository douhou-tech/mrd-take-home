package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.test.randomString
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FetchRestaurantByIdUseCaseTest {

    private val repository = mockk<RestaurantsRepository>(relaxed = true)
    private val logger = mockk<Logger>(relaxed = true)
    private val subject = FetchRestaurantByIdUseCase(
        repository = repository,
        logger = logger
    )

    @Test
    fun `when invoke then call repository should be called with correct parameters`() = runTest {
        val expectedId = randomString(10)

        subject(expectedId)

        coVerify { repository.getRestaurant(eq(expectedId)) }
    }

    @Test
    fun `when invoke and success then success result should be returned`() = runTest {
        val expectedId = randomString(10)
        val data = mockk<Restaurant>(relaxed = true).apply {
            every { id } returns expectedId
        }
        coEvery { repository.getRestaurant(eq(expectedId)) } returns data

        val actual = subject(expectedId)

        actual shouldBeEqual EitherResult.Success(data)
    }

    @Test
    fun `when invoke and exception is thrown then unknown error result should be returned`() = runTest {
        coEvery { repository.getRestaurant(any()) } throws Exception("Test exception")

        val actual = subject(randomString(10))

        actual shouldBeEqual EitherResult.Error(FetchRestaurantByIdUseCase.Error.Unknown)
    }
}