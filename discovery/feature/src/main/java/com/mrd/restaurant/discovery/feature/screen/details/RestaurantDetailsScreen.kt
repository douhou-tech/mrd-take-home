package com.mrd.restaurant.discovery.feature.screen.details

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import com.mrd.restaurant.discovery.feature.shared.container.MrDScreenScaffold

@Composable
fun RestaurantDetailsScreen(
    viewModel: RestaurantDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MrDScreenScaffold {
        when (val state = uiState) {
            is RestaurantDetailsUiState.Error -> Unit
            RestaurantDetailsUiState.Loading -> Unit
            is RestaurantDetailsUiState.Success -> {
                val restaurant = state.restaurant
                Text(
                    modifier = Modifier.clickable {
                        viewModel.onIntent(RestaurantDetailsIntent.Back)
                    },
                    text = restaurant.name
                )
            }
        }
    }
}

