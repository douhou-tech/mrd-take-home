package com.mrd.restaurant.diiscovery.domain.usecase

import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.KoinTest

class FilterRestaurantsUseCaseTest : KoinTest {

    val restaurantsRepository = mockk<RestaurantsRepository>()
    val filterRestaurantsUseCase = FilterRestaurantsUseCase(restaurantsRepository)

    @Test
    fun `when invoke then call repository should be called with correct parameters`() = runTest {
        val filters = mockk<RestaurantFilters>()
        val pagination = mockk<Pagination>()

        filterRestaurantsUseCase(filters, pagination)

        coVerify { restaurantsRepository.filterRestaurants(eq(filters), eq(pagination)) }
    }

}
