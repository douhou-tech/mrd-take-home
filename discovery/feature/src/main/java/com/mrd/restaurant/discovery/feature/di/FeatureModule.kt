package com.mrd.restaurant.discovery.feature.di

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.mrd.restaurant.discovery.feature.favourite.FavouriteRestaurantDelegate
import com.mrd.restaurant.discovery.feature.favourite.FavouriteRestaurantDelegateImpl
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
    single<FavouriteRestaurantDelegate> {
        FavouriteRestaurantDelegateImpl(
            toggleFavouriteRestaurantUseCase = get(),
            restaurantsRepository = get()
        )
    }

    viewModelOf(::RestaurantDiscoveryViewModel)
    viewModelOf(::RestaurantDetailsViewModel)

    navigation<FeatureNavKey.RestaurantDiscovery> {
        val sharedTransitionScope = LocalSharedTransitionScope.current
        val visibilityScope = LocalNavAnimatedContentScope.current
        RestaurantDiscoveryScreen(
            viewModel = koinViewModel(),
            sharedTransitionScope = sharedTransitionScope,
            visibilityScope = visibilityScope
        )
    }

    navigation<FeatureNavKey.RestaurantDetails.ByData> { args ->
        val sharedTransitionScope = LocalSharedTransitionScope.current
        val visibilityScope = LocalNavAnimatedContentScope.current
        RestaurantDetailsScreen(
            viewModel = koinViewModel {
                parametersOf(RestaurantDetailsViewModel.Params.ByData(args.restaurant))
            },
            sharedTransitionScope = sharedTransitionScope,
            visibilityScope = visibilityScope
        )
    }

    navigation<FeatureNavKey.RestaurantDetails.ById> { args ->
        val restaurantId = args.restaurantId
        val sharedTransitionScope = LocalSharedTransitionScope.current
        val visibilityScope = LocalNavAnimatedContentScope.current
        RestaurantDetailsScreen(
            viewModel = koinViewModel {
                parametersOf(RestaurantDetailsViewModel.Params.ById(restaurantId))
            },
            sharedTransitionScope = sharedTransitionScope,
            visibilityScope = visibilityScope
        )
    }
}
