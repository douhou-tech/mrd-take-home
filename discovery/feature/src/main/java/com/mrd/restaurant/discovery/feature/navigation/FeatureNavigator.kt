package com.mrd.restaurant.discovery.feature.navigation

import androidx.navigation3.runtime.NavKey
import com.mrd.restaurant.discovery.core.navigation.BackNavigator
import kotlinx.serialization.Serializable

@Serializable
sealed interface FeatureNavKey : NavKey {
    @Serializable
    data object RestaurantDiscovery : FeatureNavKey
    @Serializable
    data class RestaurantDetails(val id: String) : FeatureNavKey
}

interface  FeatureNavigator: BackNavigator {
    fun <TRoute: FeatureNavKey> navigateTo(route: TRoute)
}