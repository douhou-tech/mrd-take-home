package com.mrd.restaurant.discovery.demo.navigation

import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatchResult
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import com.mrd.restaurant.discovery.ui.navigation.BackNavigator
import com.mrd.restaurant.discovery.ui.navigation.DeepLinkNavigator

internal class AppNavigator(
    startDestination: NavKey,
    private val deepLinkMatchers: List<DeepLinkMatcher<NavKey, *>>,
    private val logger: Logger
): FeatureNavigator, BackNavigator, DeepLinkNavigator {
    val backStack = mutableStateListOf<NavKey>(startDestination)

    override fun navigateBack() {
        backStack.removeLastOrNull()
    }

    override fun <TRoute : FeatureNavKey> navigateTo(route: TRoute) {
        backStack.add(route)
    }

    override fun handleDeepLink(intent: Intent) {
        val uri = intent.data?.toString() ?: return
        try {
            logger.log("Handling deep link: $uri")

            val request = DeepLinkRequest(uri)
            val matchResult = deepLinkMatchers.firstNotNullOfOrNull { matcher ->
                matcher.match(request)
            } ?: return
            val stack = if (matchResult is BackStackMatchResult<*, *>) {
                @Suppress("UNCHECKED_CAST")
                matchResult.backStack as List<NavKey>
            } else {
                val key = matchResult.key
                if (key != FeatureNavKey.RestaurantDiscovery) {
                    listOf(FeatureNavKey.RestaurantDiscovery, key)
                } else {
                    listOf(FeatureNavKey.RestaurantDiscovery)
                }
            }

            backStack.clear()
            backStack.addAll(stack)
        } catch (ex: Exception) {
            logger.error("Failed to handle deep link to: $uri", ex)
        }
    }

}
