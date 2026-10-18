package com.mrd.restaurant.discovery.feature.di

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.screen.browse.RestaurantDiscoveryScreen
import com.mrd.restaurant.discovery.feature.screen.browse.RestaurantDiscoveryViewModel
import com.mrd.restaurant.discovery.feature.screen.details.RestaurantDetailsScreen
import com.mrd.restaurant.discovery.feature.screen.details.RestaurantDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("SharedTransitionScope not found")
}

@OptIn(KoinExperimentalAPI::class)
val restaurantDiscoveryFeatureModule = module {
    viewModelOf(::RestaurantDiscoveryViewModel)
    navigation<FeatureNavKey.RestaurantDiscovery> {
        val sharedTransitionScope = LocalSharedTransitionScope.current
        val visibilityScope = LocalNavAnimatedContentScope.current
        RestaurantDiscoveryScreen(
            viewModel = koinViewModel(),
            sharedTransitionScope = sharedTransitionScope,
            visibilityScope = visibilityScope
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
val restaurantDetailsFeatureModule = module {
    viewModelOf(::RestaurantDetailsViewModel)
    navigation<FeatureNavKey.RestaurantDetails> { args ->
        val restaurantId = args.id
        val sharedTransitionScope = LocalSharedTransitionScope.current
        val visibilityScope = LocalNavAnimatedContentScope.current
        RestaurantDetailsScreen(
            viewModel = koinViewModel {
                parametersOf(restaurantId)
            },
            sharedTransitionScope = sharedTransitionScope,
            visibilityScope = visibilityScope
        )
    }
}
