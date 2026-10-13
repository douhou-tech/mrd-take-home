package com.mrd.restaurant.discovery.feature.screen.details

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.usecase.FetchRestaurantByIdUseCase
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RestaurantDetailsViewModel(
    private val navigator: FeatureNavigator,
    private val fetchRestaurantByIdUseCase: FetchRestaurantByIdUseCase,
    private val restaurantId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestaurantDetailsUiState>(RestaurantDetailsUiState.Loading)
    val uiState: StateFlow<RestaurantDetailsUiState> = _uiState

    init {
        loadRestaurant()
    }

    fun onIntent(intent: RestaurantDetailsIntent) {
        when (intent) {
            RestaurantDetailsIntent.Back -> onBack()
        }
    }

    private fun onBack() {
        navigator.navigateBack()
    }

    private fun loadRestaurant() {
        viewModelScope.launch {
            when (val result = fetchRestaurantByIdUseCase(restaurantId)) {
                is EitherResult.Success -> {
                    _uiState.update {
                        RestaurantDetailsUiState.Success(
                            restaurant = result.data
                        )
                    }
                }
                is EitherResult.Error -> {
                    _uiState.update {
                        RestaurantDetailsUiState.Error(
                            message = "Failed to load restaurant"
                        )
                    }
                }
            }
        }
    }
}

sealed interface RestaurantDetailsIntent {
    data object Back : RestaurantDetailsIntent
}

sealed interface RestaurantDetailsUiState {
    data object Loading : RestaurantDetailsUiState

    data class Success(
        val restaurant: Restaurant,
    ) : RestaurantDetailsUiState

    data class Error(
        val message: String,
    ) : RestaurantDetailsUiState
}
