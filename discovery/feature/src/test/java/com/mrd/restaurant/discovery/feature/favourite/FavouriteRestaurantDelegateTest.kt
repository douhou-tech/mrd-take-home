package com.mrd.restaurant.discovery.feature.favourite

import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.domain.usecase.ToggleFavouriteRestaurantUseCase
import com.mrd.test.randomString
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavouriteRestaurantDelegateTest {

    private val toggleFavouriteRestaurantUseCase = mockk<ToggleFavouriteRestaurantUseCase>(relaxed = true)
    private val restaurantsRepository = mockk<RestaurantsRepository>(relaxed = true)
    private val subject = FavouriteRestaurantDelegateImpl(
        toggleFavouriteRestaurantUseCase = toggleFavouriteRestaurantUseCase,
        restaurantsRepository = restaurantsRepository
    )

    @Test
    fun `when toggleFavourite called then repository setFavourite is invoked immediately with optimistic state`() = runTest {
        val restaurantId = randomString(10)

        subject.toggleFavourite(
            scope = this,
            restaurantId = restaurantId,
            favourite = false
        )

        testScheduler.advanceTimeBy(10)

        coVerify {
            restaurantsRepository.setFavourite(eq(restaurantId), eq(true))
        }
    }

    @Test
    fun `when toggleFavourite called and debounce passes then use case is executed`() = runTest {
        val restaurantId = randomString(10)

        subject.toggleFavourite(
            scope = this,
            restaurantId = restaurantId,
            favourite = false
        )

        testScheduler.advanceTimeBy(350)

        coVerify {
            favouriteRestaurantUseCase(eq(restaurantId), eq(true))
        }
    }

    @Test
    fun `when use case fails then repository setFavourite is reverted to original state`() = runTest {
        val restaurantId = randomString(10)
        coEvery { favouriteRestaurantUseCase(any(), any()) } returns EitherResult.Error(ToggleFavouriteRestaurantUseCase.Error.Unknown)

        subject.toggleFavourite(
            scope = this,
            restaurantId = restaurantId,
            favourite = true
        )

        testScheduler.advanceTimeBy(350)

        coVerify {
            restaurantsRepository.setFavourite(eq(restaurantId), eq(false))
        }
    }
}
