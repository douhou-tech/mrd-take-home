package com.mrd.restaurant.discovery.feature.screen.details

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrd.restaurant.discovery.feature.shared.component.RemoteImage
import com.mrd.restaurant.discovery.feature.shared.container.MrDScreenScaffold

@Composable
fun RestaurantDetailsScreen(
    viewModel: RestaurantDetailsViewModel,
    sharedTransitionScope: SharedTransitionScope,
    visibilityScope: AnimatedContentScope
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MrDScreenScaffold {
        when (val state = uiState) {
            is RestaurantDetailsUiState.Error -> Unit
            RestaurantDetailsUiState.Loading -> Unit
            is RestaurantDetailsUiState.Success -> RestaurantDetailsContent(
                sharedTransitionScope = sharedTransitionScope,
                visibilityScope = visibilityScope,
                state = state
            )
        }
    }
}

@Composable
private fun RestaurantDetailsContent(
    sharedTransitionScope: SharedTransitionScope,
    visibilityScope: AnimatedContentScope,
    state: RestaurantDetailsUiState.Success
) {
    val restaurant = state.restaurant

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            with(sharedTransitionScope) {
                RemoteImage(
                    modifier = Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(key = restaurant.id),
                        animatedVisibilityScope = visibilityScope
                    ),
                    imageUrl = restaurant.imageUrl
                )
            }
        }

    }
}

