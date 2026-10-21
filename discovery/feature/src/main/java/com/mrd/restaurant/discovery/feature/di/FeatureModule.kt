package com.mrd.restaurant.discovery.feature.di

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.withBackStack
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.mrd.restaurant.discovery.feature.favourite.FavouriteRestaurantDelegate
import com.mrd.restaurant.discovery.feature.favourite.FavouriteRestaurantDelegateImpl
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.screen.browse.RestaurantDiscoveryScreen
import com.mrd.restaurant.discovery.feature.screen.browse.RestaurantDiscoveryViewModel
import com.mrd.restaurant.discovery.feature.screen.details.RestaurantDetailsScreen
import com.mrd.restaurant.discovery.feature.screen.details.RestaurantDetailsViewModel
import kotlinx.serialization.serializer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
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

    single<DeepLinkMatcher<NavKey, *>>(named(FeatureModuleQualifiers.RESTAURANT_DETAILS_DEEPLINK_MATCHER)) {
        val urlWithoutScheme = "staging.mrd.likeabite.com/restaurant/details/{restaurantId}"
        val schemes = listOf("http", "https")
        val matchers = schemes.map { scheme ->
            UriDeepLinkMatcher(
                uriPattern = DeepLinkUri("${scheme}://${urlWithoutScheme}"),
                serializer = serializer<FeatureNavKey.RestaurantDetails.ById>()
            )
        }.map { matcher ->
            matcher.withBackStack { matchResult ->
                val route = matchResult.key
                listOf(
                    FeatureNavKey.RestaurantDiscovery,
                    route
                )
            }
        }

        object : DeepLinkMatcher<NavKey, DeepLinkMatcher.MatchResult<NavKey>>() {
            override fun matchRequest(request: DeepLinkRequest): MatchResult<NavKey>? {
                return matchers.firstNotNullOfOrNull { matcher ->
                    matcher.match(request)
                }
            }
        }
    }
}

object FeatureModuleQualifiers {

    const val RESTAURANT_DETAILS_DEEPLINK_MATCHER = "restaurant_details_deeplink_matcher"

}