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
    fun `when toggleFavourite called and debounce passes then use case is executed`() = runTest {
        val restaurantId = randomString(10)

        subject.toggleFavourite(
            restaurantId = restaurantId
        )

        Thread.sleep(400)

        coVerify {
            toggleFavouriteRestaurantUseCase(eq(restaurantId))
        }
    }
}
