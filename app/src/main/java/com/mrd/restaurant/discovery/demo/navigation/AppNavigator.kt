package com.mrd.restaurant.discovery.demo.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import com.mrd.restaurant.discovery.core.navigation.BackNavigator
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator

internal class AppNavigator(startDestination: NavKey): FeatureNavigator, BackNavigator {
    val backStack = mutableStateListOf<NavKey>(startDestination)

    override fun navigateBack() {
        backStack.removeLastOrNull()
    }

    override fun <TRoute : FeatureNavKey> navigateTo(route: TRoute) {
        backStack.add(route)
    }

}
