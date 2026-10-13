package com.mrd.restaurant.discovery.domain.usecase

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

    private val restaurantsRepository = mockk<RestaurantsRepository>(relaxed = true)
    private val subject = FetchRestaurantByIdUseCase(restaurantsRepository)

    @Test
    fun `when invoke then call repository should be called with correct parameters`() = runTest {
        val expectedId = randomString(10)

        subject(expectedId)

        coVerify { restaurantsRepository.getRestaurant(eq(expectedId)) }
    }

    @Test
    fun `when invoke and success then success result should be returned`() = runTest {
        val expectedId = randomString(10)
        val data = mockk<Restaurant>(relaxed = true).apply {
            every { id } returns expectedId
        }
        coEvery { restaurantsRepository.getRestaurant(eq(expectedId)) } returns data

        val actual = subject(expectedId)

        actual shouldBeEqual EitherResult.Success(data)
    }

    @Test
    fun `when invoke and exception is thrown then unknown error result should be returned`() = runTest {
        coEvery { restaurantsRepository.getRestaurant(any()) } throws Exception("Test exception")

        val actual = subject(randomString(10))

        actual shouldBeEqual EitherResult.Error(FetchRestaurantByIdUseCase.Error.Unknown)
    }
}