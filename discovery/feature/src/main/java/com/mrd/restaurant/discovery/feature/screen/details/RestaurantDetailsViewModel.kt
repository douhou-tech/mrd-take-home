package com.mrd.restaurant.discovery.feature.screen.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.usecase.FavouriteRestaurantUseCase
import com.mrd.restaurant.discovery.domain.usecase.FetchRestaurantByIdUseCase
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class RestaurantDetailsViewModel(
    private val navigator: FeatureNavigator,
    private val fetchRestaurantByIdUseCase: FetchRestaurantByIdUseCase,
    private val favouriteRestaurantUseCase: FavouriteRestaurantUseCase,
    private val restaurantId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestaurantDetailsUiState>(RestaurantDetailsUiState.Loading)
    val uiState: StateFlow<RestaurantDetailsUiState> = _uiState

    private val favouriteFlow = MutableStateFlow<Boolean?>(null)

    init {
        loadRestaurant()
        setupFavouriteFlow()
    }

    private fun setupFavouriteFlow() {
        viewModelScope.launch {
            favouriteFlow
                .filterNotNull()
                .debounce(300L.milliseconds)
                .collectLatest(::favourite)
        }
    }

    fun onIntent(intent: RestaurantDetailsIntent) {
        when (intent) {
            RestaurantDetailsIntent.Back -> onBack()
            RestaurantDetailsIntent.Favourite -> onFavourite()
        }
    }

    private fun onBack() {
        navigator.navigateBack()
    }

    private fun onFavourite() {
        val currentState = _uiState.value
        if (currentState !is RestaurantDetailsUiState.Success) return

        val currentRestaurant = currentState.restaurant
        val newFavourite = !currentRestaurant.favourite

        _uiState.update { state ->
            if (state is RestaurantDetailsUiState.Success) {
                state.copy(restaurant = state.restaurant.copy(favourite = newFavourite, wishlisted = newFavourite))
            } else state
        }

        favouriteFlow.value = newFavourite
    }

    private suspend fun favourite(favourite: Boolean) {
        when (favouriteRestaurantUseCase(restaurantId, favourite)) {
            is EitherResult.Success -> Unit
            is EitherResult.Error -> {
                _uiState.update { state ->
                    if (state is RestaurantDetailsUiState.Success) {
                        state.copy(restaurant = state.restaurant.copy(favourite = !favourite, wishlisted = !favourite))
                    } else state
                }
            }
        }
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
    data object Favourite : RestaurantDetailsIntent
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
