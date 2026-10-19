package com.mrd.restaurant.discovery.feature.screen.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.usecase.FetchRestaurantByIdUseCase
import com.mrd.restaurant.discovery.feature.favourite.FavouriteRestaurantDelegate
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RestaurantDetailsViewModel(
    private val navigator: FeatureNavigator,
    private val fetchRestaurantByIdUseCase: FetchRestaurantByIdUseCase,
    private val favouriteRestaurantDelegate: FavouriteRestaurantDelegate,
    private val params: Params
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestaurantDetailsUiState>(RestaurantDetailsUiState.Loading)
    val uiState: StateFlow<RestaurantDetailsUiState> = _uiState

    val restaurantId: String

    init {
        setupFavouritesChangeFlow()
        when (params) {
            is Params.ByData -> {
                restaurantId = params.restaurant.id
                _uiState.update {
                    RestaurantDetailsUiState.Success(
                        restaurant = params.restaurant,
                        favourite = params.restaurant.favourite
                    )
                }
            }
            is Params.ById -> {
                restaurantId = params.restaurantId
                loadRestaurant()
            }
        }
    }

    private fun setupFavouritesChangeFlow() {
        viewModelScope.launch {
            favouriteRestaurantDelegate.favourites
                .collect { favourites ->
                    val favourite = favourites.contains(restaurantId)
                    _uiState.update { state ->
                        if (state is RestaurantDetailsUiState.Success) {
                            state.copy(favourite = favourite)
                        } else state
                    }
                }
        }
    }

    fun onIntent(intent: RestaurantDetailsIntent) {
        when (intent) {
            RestaurantDetailsIntent.Back -> onBack()
            RestaurantDetailsIntent.ToggleFavourite -> onToggleFavourite()
        }
    }

    private fun onBack() {
        navigator.navigateBack()
    }

    private fun onToggleFavourite() {
        val state = _uiState.value
        if (state !is RestaurantDetailsUiState.Success) return

        favouriteRestaurantDelegate.toggleFavourite(
            scope = viewModelScope,
            restaurantId = restaurantId,
            favourite = state.favourite
        )
    }

    private fun loadRestaurant() {
        viewModelScope.launch {
            when (val result = fetchRestaurantByIdUseCase(restaurantId)) {
                is EitherResult.Success -> {
                    val restaurant = result.data
                    _uiState.update {
                        RestaurantDetailsUiState.Success(
                            restaurant = restaurant,
                            favourite = restaurant.favourite
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

    sealed interface Params {
        data class ById(val restaurantId: String) : Params
        data class ByData(val restaurant: Restaurant) : Params
    }
}

sealed interface RestaurantDetailsIntent {
    data object Back : RestaurantDetailsIntent
    data object ToggleFavourite : RestaurantDetailsIntent
}

sealed interface RestaurantDetailsUiState {
    data object Loading : RestaurantDetailsUiState

    data class Success(
        val restaurant: Restaurant,
        val favourite: Boolean
    ) : RestaurantDetailsUiState

    data class Error(
        val message: String,
    ) : RestaurantDetailsUiState
}
