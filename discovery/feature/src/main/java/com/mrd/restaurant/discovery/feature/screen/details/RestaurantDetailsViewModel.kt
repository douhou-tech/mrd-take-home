package com.mrd.restaurant.discovery.feature.screen.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.model.MenuItem
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.usecase.FetchMenuItemsByRestaurantIdUseCase
import com.mrd.restaurant.discovery.domain.usecase.FetchRestaurantByIdUseCase
import com.mrd.restaurant.discovery.feature.favourite.FavouriteRestaurantDelegate
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class RestaurantDetailsViewModel(
    private val navigator: FeatureNavigator,
    private val fetchRestaurantByIdUseCase: FetchRestaurantByIdUseCase,
    private val fetchMenuItemsByRestaurantIdUseCase: FetchMenuItemsByRestaurantIdUseCase,
    private val favouriteRestaurantDelegate: FavouriteRestaurantDelegate,
    private val params: Params
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestaurantDetailsUiState>(RestaurantDetailsUiState.Loading)
    val uiState: StateFlow<RestaurantDetailsUiState> = _uiState

    val restaurantId: String
    private var currentFavourites: Set<String>? = null

    init {
        setupFavouritesChangeFlow()
        when (params) {
            is Params.ByData -> {
                restaurantId = params.restaurant.id
                val favourite = currentFavourites?.contains(restaurantId) ?: params.restaurant.favourite
                _uiState.update {
                    RestaurantDetailsUiState.Success(
                        data = RestaurantDetailsData(
                            restaurant = params.restaurant,
                            favourite = favourite
                        )
                    )
                }
                loadMenuItems()
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
                    currentFavourites = favourites
                    val favourite = favourites.contains(restaurantId)
                    _uiState.update { state ->
                        updateStateWithFavourite(state, favourite)
                    }
                }
        }
    }

    private fun updateStateWithFavourite(
        state: RestaurantDetailsUiState,
        isFavourite: Boolean
    ): RestaurantDetailsUiState {
        return when (state) {
            is RestaurantDetailsUiState.Success -> state.copy(data = state.data.copy(favourite = isFavourite))
            is RestaurantDetailsUiState.LoadingMenu -> state.copy(data = state.data.copy(favourite = isFavourite))
            is RestaurantDetailsUiState.MenuError -> state.copy(data = state.data.copy(favourite = isFavourite))
            else -> state
        }
    }

    fun onIntent(intent: RestaurantDetailsIntent) {
        when (intent) {
            RestaurantDetailsIntent.Back -> onBack()
            RestaurantDetailsIntent.ToggleFavourite -> onToggleFavourite()
            RestaurantDetailsIntent.Retry -> onRetry()
        }
    }

    private fun onBack() {
        navigator.navigateBack()
    }

    private fun onToggleFavourite() {
        val state = _uiState.value
        if (state !is RestaurantDetailsUiState.PartialDataState) return

        favouriteRestaurantDelegate.toggleFavourite(
            restaurantId = restaurantId
        )
    }

    private fun onRetry() {
        when (_uiState.value) {
            is RestaurantDetailsUiState.Error -> loadRestaurant()
            is RestaurantDetailsUiState.MenuError -> loadMenuItems()
            else -> Unit
        }
    }

    private fun loadRestaurant() {
        viewModelScope.launch {
            _uiState.update { RestaurantDetailsUiState.Loading }

            val (restaurant, menuItems) = loadRestaurantAsync()
            val favourite = restaurant?.let { currentFavourites?.contains(it.id) ?: it.favourite } ?: false
            val state = when {
                restaurant != null && menuItems != null -> {
                    RestaurantDetailsUiState.Success(
                        data = RestaurantDetailsData(
                            restaurant = restaurant,
                            favourite = favourite,
                            menuItems = menuItems
                        )
                    )
                }
                restaurant != null -> {
                    RestaurantDetailsUiState.MenuError(
                        data = RestaurantDetailsData(
                            restaurant = restaurant,
                            favourite = favourite
                        ),
                        message = "Failed to load menu items"
                    )
                }
                else -> {
                    RestaurantDetailsUiState.Error(
                        message = "Failed to load restaurant details"
                    )
                }
            }

            _uiState.update { state }
        }
    }


    private suspend fun loadRestaurantAsync(): Pair<Restaurant?, List<MenuItem>?> = supervisorScope{
        val restaurantDeferred = async {
            fetchRestaurantByIdUseCase(restaurantId)
        }
        val menuItemsDeferred = async {
            fetchMenuItemsByRestaurantIdUseCase(restaurantId)
        }
        val restaurantResult = restaurantDeferred.await()
        val menuItemsResult = menuItemsDeferred.await()

        val restaurant = when {
            restaurantResult is EitherResult.Success -> restaurantResult.data
            else -> null
        }
        val menuItems = when {
            menuItemsResult is EitherResult.Success -> menuItemsResult.data
            else -> null
        }

        return@supervisorScope restaurant to menuItems
    }
    
    private fun loadMenuItems() {
        val state = _uiState.value
        if (state !is RestaurantDetailsUiState.PartialDataState) return

        _uiState.update {
            RestaurantDetailsUiState.LoadingMenu(
                data = state.data
            )
        }

        viewModelScope.launch {
            when (val menuItemsResult = fetchMenuItemsByRestaurantIdUseCase(restaurantId)) {
                is EitherResult.Success -> {
                    _uiState.update { currentState ->
                        val currentData = (currentState as? RestaurantDetailsUiState.PartialDataState)?.data ?: state.data
                        RestaurantDetailsUiState.Success(
                            data = currentData.copy(
                                menuItems = menuItemsResult.data
                            )
                        )
                    }
                }
                is EitherResult.Error -> {
                    _uiState.update { currentState ->
                        val currentData = (currentState as? RestaurantDetailsUiState.PartialDataState)?.data ?: state.data
                        RestaurantDetailsUiState.MenuError(
                            data = currentData,
                            message = "Failed to load menu items"
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
    data object Retry : RestaurantDetailsIntent
}

sealed interface RestaurantDetailsUiState {

    sealed interface PartialDataState : RestaurantDetailsUiState {
        val data: RestaurantDetailsData
    }

    data object Loading : RestaurantDetailsUiState
    data class LoadingMenu(
        override val data: RestaurantDetailsData
    ) : PartialDataState

    data class Success(
        override val data: RestaurantDetailsData
    ) : PartialDataState

    data class Error(
        val message: String,
    ) : RestaurantDetailsUiState

    data class MenuError(
        val message: String,
        override val data: RestaurantDetailsData,
    ) : PartialDataState
}

data class RestaurantDetailsData(
    val restaurant: Restaurant,
    val favourite: Boolean = restaurant.favourite,
    val menuItems: List<MenuItem> = emptyList(),
)