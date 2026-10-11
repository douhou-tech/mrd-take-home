package com.mrd.restaurant.discovery.demo

import android.content.Context
import com.mrd.restaurant.diiscovery.domain.di.restaurantDiscoveryDomainModule
import com.mrd.restaurant.diiscovery.domain.usecase.FilterRestaurantsUseCase
import com.mrd.restaurant.discovery.data.di.restaurantDiscoveryDataModule
import io.mockk.mockk
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import kotlin.test.assertNotNull

class KoinModuleTest : KoinTest {

    @Test
    fun `verify FilterRestaurantsUseCase is resolved with RestaurantsRepositoryImpl`() {
        val mockContext = mockk<Context>(relaxed = true)
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(restaurantDiscoveryDomainModule, restaurantDiscoveryDataModule)
        }

        val useCase = koinApp.koin.get<FilterRestaurantsUseCase>()
        assertNotNull(useCase)
    }
}
