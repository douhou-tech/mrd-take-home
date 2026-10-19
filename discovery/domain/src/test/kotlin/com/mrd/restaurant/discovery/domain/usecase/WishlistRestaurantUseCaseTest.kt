package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.test.randomString
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random

class WishlistRestaurantUseCaseTest {


    private val repository = mockk<RestaurantsRepository>(relaxed = true)
    private val logger = mockk<Logger>(relaxed = true)
    private val subject = WishlistRestaurantUseCase(
        repository = repository,
        logger = logger
    )

    @Test
    fun `when invoke then repository is called with correct arguments`() = runTest {
        val restaurantId = randomString(10)
        val wishlisted = Random.nextBoolean()

        subject.invoke(restaurantId, wishlisted)

        coVerify {
            repository.setWishlisted(eq(restaurantId), eq(wishlisted))
        }
    }


    @Test
    fun `when invoke and wishlisted is successful then success result should be returned`() = runTest {
        val restaurantId = randomString(10)
        val wishlisted = Random.nextBoolean()

        val actual = subject.invoke(restaurantId, wishlisted)

        actual shouldBeEqual EitherResult.Success(Unit)
    }

    @Test
    fun `when invoke and repository throws an error then unknown error result should be returned`() = runTest {
        val restaurantId = randomString(10)
        val wishlisted = Random.nextBoolean()
        coEvery {
            repository.setWishlisted(any(), any())
        } throws Exception("Test exception")


        val actual = subject.invoke(restaurantId, wishlisted)

        actual shouldBeEqual EitherResult.Error(WishlistRestaurantUseCase.Error.Unknown)
    }
}