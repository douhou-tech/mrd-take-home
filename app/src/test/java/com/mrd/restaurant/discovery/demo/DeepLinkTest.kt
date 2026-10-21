package com.mrd.restaurant.discovery.demo

import android.content.Context
import android.content.Intent
import com.mrd.restaurant.discovery.data.di.restaurantDiscoveryDataModule
import com.mrd.restaurant.discovery.demo.di.appModule
import com.mrd.restaurant.discovery.demo.navigation.AppNavigator
import com.mrd.restaurant.discovery.domain.restaurantDiscoveryDomainModule
import com.mrd.restaurant.discovery.feature.di.restaurantDiscoveryFeatureModule
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import kotlin.test.assertEquals

class DeepLinkTest : KoinTest {

    @Before
    fun setup() {
        mockkStatic(android.net.Uri::class)
        every { android.net.Uri.decode(any()) } answers { firstArg() }
        every { android.net.Uri.encode(any()) } answers { firstArg() }
        every { android.net.Uri.encode(any(), any()) } answers { firstArg() }
        every { android.net.Uri.parse(any()) } answers {
            val uriStr = firstArg<String>()
            val mockUri = mockk<android.net.Uri>(relaxed = true)
            val parts = uriStr.split("://", limit = 2)
            val scheme = if (parts.size > 1) parts[0] else null
            val rest = if (parts.size > 1) parts[1] else parts[0]
            val pathStart = rest.indexOf('/')
            val hostAndPort = if (pathStart >= 0) rest.substring(0, pathStart) else rest
            val pathAndQuery = if (pathStart >= 0) rest.substring(pathStart) else ""
            val queryStart = pathAndQuery.indexOf('?')
            val rawPath = if (queryStart >= 0) pathAndQuery.substring(0, queryStart) else pathAndQuery
            val rawQuery = if (queryStart >= 0) pathAndQuery.substring(queryStart + 1) else null

            val segments = rawPath.split("/").filter { it.isNotEmpty() }

            every { mockUri.scheme } returns scheme
            every { mockUri.authority } returns hostAndPort
            every { mockUri.host } returns hostAndPort
            every { mockUri.path } returns rawPath
            every { mockUri.pathSegments } returns segments
            every { mockUri.query } returns rawQuery
            every { mockUri.toString() } returns uriStr
            mockUri
        }
    }

    @Test
    fun `verify deep link matches https staging mrd likeabite com and navigates to details`() {
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

        val navigator = koinApp.koin.get<AppNavigator>()
        val mockIntent = mockk<Intent>(relaxed = true)
        every { mockIntent.data?.toString() } returns "https://staging.mrd.likeabite.com/restaurant/details/cpt-cbd-003"

        navigator.handleDeepLink(mockIntent)

        assertEquals(2, navigator.backStack.size)
        assertEquals(FeatureNavKey.RestaurantDiscovery, navigator.backStack[0])
        val detailsKey = navigator.backStack[1] as FeatureNavKey.RestaurantDetails.ById
        assertEquals("cpt-cbd-003", detailsKey.restaurantId)
    }

    @Test
    fun `verify deep link matches http staging mrd likeabite com and navigates to details`() {
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

        val navigator = koinApp.koin.get<AppNavigator>()
        val mockIntent = mockk<Intent>(relaxed = true)
        every { mockIntent.data?.toString() } returns "http://staging.mrd.likeabite.com/restaurant/details/cpt-cbd-003"

        navigator.handleDeepLink(mockIntent)

        assertEquals(2, navigator.backStack.size)
        assertEquals(FeatureNavKey.RestaurantDiscovery, navigator.backStack[0])
        val detailsKey = navigator.backStack[1] as FeatureNavKey.RestaurantDetails.ById
        assertEquals("cpt-cbd-003", detailsKey.restaurantId)
    }
}
