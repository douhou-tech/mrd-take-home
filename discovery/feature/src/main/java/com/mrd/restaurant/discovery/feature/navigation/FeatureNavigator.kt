package com.mrd.restaurant.discovery.feature.navigation

import androidx.navigation3.runtime.NavKey
import com.mrd.restaurant.discovery.core.navigation.BackNavigator
import com.mrd.restaurant.discovery.domain.model.Restaurant
import kotlinx.serialization.Serializable

@Serializable
sealed interface FeatureNavKey : NavKey {
    @Serializable
    data object RestaurantDiscovery : FeatureNavKey
    @Serializable
    sealed interface RestaurantDetails : FeatureNavKey {
        @Serializable
        data class ById(val restaurantId: String): RestaurantDetails
        @Serializable
        data class ByData(val restaurant: Restaurant): RestaurantDetails
    }

}

interface  FeatureNavigator: BackNavigator {
    fun <TRoute: FeatureNavKey> navigateTo(route: TRoute)
}