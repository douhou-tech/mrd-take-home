package com.mrd.restaurant.discovery.demo

import android.content.Context
import com.mrd.restaurant.discovery.domain.restaurantDiscoveryDomainModule
import com.mrd.restaurant.discovery.domain.usecase.FilterRestaurantsUseCase
import com.mrd.restaurant.discovery.data.di.restaurantDiscoveryDataModule
import com.mrd.restaurant.discovery.demo.di.appModule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.every
import org.junit.Before
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import kotlin.test.assertNotNull

import androidx.lifecycle.SavedStateHandle
import com.mrd.restaurant.discovery.feature.di.restaurantDiscoveryFeatureModule
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import com.mrd.restaurant.discovery.feature.screen.browse.RestaurantDiscoveryViewModel
import org.koin.core.parameter.parametersOf

class KoinModuleTest : KoinTest {

    @Before
    fun setup() {
        mockkStatic(android.net.Uri::class)
        val mockUri = mockk<android.net.Uri>(relaxed = true)
        every { android.net.Uri.parse(any()) } returns mockUri
    }

    @Test
    fun `verify FilterRestaurantsUseCase is resolved with RestaurantsRepositoryImpl`() {
        val mockContext = mockk<Context>(relaxed = true)
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(appModule, restaurantDiscoveryDomainModule, restaurantDiscoveryDataModule)
        }

        val useCase = koinApp.koin.get<FilterRestaurantsUseCase>()
        assertNotNull(useCase)
    }

    @Test
    fun `verify FeatureNavigator resolution`() {
        val mockContext = mockk<Context>(relaxed = true)
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(
                appModule,
                restaurantDiscoveryFeatureModule
            )
        }

        val navigator = koinApp.koin.get<FeatureNavigator>()
        assertNotNull(navigator)
    }

    @Test
    fun `verify RestaurantDiscoveryViewModel resolution`() {
        val mockContext = mockk<Context>(relaxed = true)
        val koinApp = koinApplication {
            androidContext(mockContext)
            modules(
                appModule,
                restaurantDiscoveryDomainModule,
                restaurantDiscoveryDataModule,
                restaurantDiscoveryFeatureModule
            )
        }

        val viewModel = koinApp.koin.get<RestaurantDiscoveryViewModel> {
            parametersOf(SavedStateHandle())
        }
        assertNotNull(viewModel)
    }
}
