package com.mrd.restaurant.discovery.feature.screen.browse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.discovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.domain.usecase.FilterRestaurantsUseCase
import com.mrd.restaurant.discovery.feature.favourite.FavouriteRestaurantDelegate
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class RestaurantDiscoveryViewModel(
    private val navigator: FeatureNavigator,
    private val filterRestaurantsUseCase: FilterRestaurantsUseCase,
    private val favouriteRestaurantDelegate: FavouriteRestaurantDelegate,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var query: String
        get() = savedStateHandle[KEY_QUERY] ?: ""
        set(value) { savedStateHandle[KEY_QUERY] = value }

    private var isOpenNow: Boolean
        get() = savedStateHandle[KEY_IS_OPEN_NOW] ?: false
        set(value) { savedStateHandle[KEY_IS_OPEN_NOW] = value }

    private var currentPage: Int
        get() = savedStateHandle[KEY_CURRENT_PAGE] ?: 0
        set(value) { savedStateHandle[KEY_CURRENT_PAGE] = value }

    private var pageSize: Int
        get() = savedStateHandle[KEY_PAGE_SIZE] ?: 20
        set(value) { savedStateHandle[KEY_PAGE_SIZE] = value }

    private val _uiState = MutableStateFlow<RestaurantDiscoveryUiState>(RestaurantDiscoveryUiState.Loading)
    private val _filterState = MutableStateFlow(
        RestaurantDiscoveryFilterState(
            query = query,
            isOpenNow = isOpenNow,
            totalCount = 0
        )
    )
    val uiState: StateFlow<RestaurantDiscoveryUiState> = _uiState.asStateFlow()
    val filterState: StateFlow<RestaurantDiscoveryFilterState> = _filterState.asStateFlow()
    private val filterFlow = filterState
        .distinctUntilChanged { a, b ->
            a.query == b.query && a.isOpenNow == b.isOpenNow
        }
        .debounce(300L.milliseconds)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(1000L),
            filterState
        )

    private var observeFavouritesJob: Job? = null

    init {
        setupInputChangeFlows()
        setupFavouritesChangeFlow()
        loadRestaurants()
    }

    private fun setupInputChangeFlows() {
        viewModelScope.launch {
            launch {
                filterFlow.collectLatest {
                    loadRestaurants()
                }
            }
        }
    }

    private fun setupFavouritesChangeFlow() {
        observeFavouritesJob?.cancel()
        observeFavouritesJob = viewModelScope.launch {
            favouriteRestaurantDelegate.favourites
                .distinctUntilChanged()
                .collectLatest { favourites ->
                    _uiState.update { state ->
                        if (state is RestaurantDiscoveryUiState.Success) {
                            state.copy(
                                favourites = favourites
                            )
                        } else state
                    }
                }
        }
    }

    fun onIntent(intent: RestaurantDiscoveryIntent) {
        when (intent) {
            is RestaurantDiscoveryIntent.NavigateToDetails -> onNavigateToDetails(intent.restaurant)
            is RestaurantDiscoveryIntent.SearchQueryChanged -> onSearchQueryChange(intent.query)
            is RestaurantDiscoveryIntent.ToggleOpenNow -> onToggleOpenNow(intent.isOpenNow)
            is RestaurantDiscoveryIntent.ToggleFavourite -> onToggleFavourite(intent.restaurant)
            RestaurantDiscoveryIntent.LoadMore -> onLoadMore()
            RestaurantDiscoveryIntent.Retry -> onRetry()
        }
    }

    private fun onRetry() {
        if (_uiState.value is RestaurantDiscoveryUiState.Error) {
            loadRestaurants()
        }
    }

    private fun onNavigateToDetails(restaurant: Restaurant) {
        val state = _uiState.value
        val isFavourite = if (state is RestaurantDiscoveryUiState.Success) {
            state.favourites.contains(restaurant.id)
        } else {
            restaurant.favourite
        }
        val updatedRestaurant = restaurant.copy(favourite = isFavourite)
        navigator.navigateTo(FeatureNavKey.RestaurantDetails.ByData(updatedRestaurant))
    }

    private fun onSearchQueryChange(query: String) {
        if (this.query == query) return

        this.query = query
        currentPage = 0
        _filterState.update {
            it.copy(query = query)
        }
    }

    private fun onToggleOpenNow(isOpenNow: Boolean) {
        if (this.isOpenNow == isOpenNow) return

        this.isOpenNow = isOpenNow
        _filterState.update {
            it.copy(isOpenNow = isOpenNow)
        }
        currentPage = 0
    }

    private fun onToggleFavourite(restaurant: Restaurant) {
        val state = _uiState.value
        if (state !is RestaurantDiscoveryUiState.Success) return

        favouriteRestaurantDelegate.toggleFavourite(
            restaurantId = restaurant.id,
        )
    }

    private fun onLoadMore() {
        val uiState = _uiState.value
        if (uiState !is RestaurantDiscoveryUiState.Success) return
        val filterState = _filterState.value
        if ((currentPage + 1) * pageSize < filterState.totalCount) {
            loadMoreRestaurants(uiState)
        }
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _uiState.update {
                RestaurantDiscoveryUiState.Loading
            }

            val nextState = when (val result = filterRestaurantsUseCase(filters(), pagination())) {
                is EitherResult.Success -> {
                    val paged = result.data
                    _filterState.update {
                        it.copy(totalCount = paged.totalCount)
                    }
                    if (paged.items.isEmpty()) {
                        RestaurantDiscoveryUiState.Empty
                    } else {
                        setupFavouritesChangeFlow()
                        RestaurantDiscoveryUiState.Success(
                            restaurants = paged.items
                        )
                    }
                }
                is EitherResult.Error -> {
                    RestaurantDiscoveryUiState.Error("Failed to load restaurants")
                }
            }

            _uiState.update { nextState }
        }
    }

    fun loadMoreRestaurants(state: RestaurantDiscoveryUiState.Success) {
        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoadingMore = true
            )
            val nextState = when (val result = filterRestaurantsUseCase(filters(), pagination())) {
                is EitherResult.Success -> {
                    val paged = result.data
                    if (paged.items.isNotEmpty()) {
                        currentPage += 1
                        val restaurants = state.restaurants + paged.items
                        state.copy(
                            restaurants = restaurants,
                            isLoadingMore = false
                        )
                    } else null
                }
                else -> null
            }

           if (nextState != null) _uiState.update { nextState }
           else _uiState.update { state.copy(isLoadingMore = false) }
        }

    }

    private fun filters(): RestaurantFilters {
        val filters = buildSet {
            if (query.isNotBlank()) add(RestaurantFilter.Query(query))
            if (isOpenNow) add(RestaurantFilter.IsOpenNow)
        }
        return RestaurantFilters(
            filters = filters
        )
    }

    private fun pagination() = Pagination(page = currentPage, pageSize = pageSize)

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_IS_OPEN_NOW = "is_open_now"
        private const val KEY_CURRENT_PAGE = "current_page"
        private const val KEY_PAGE_SIZE = "page_size"
    }
}

sealed interface RestaurantDiscoveryIntent {
    data class NavigateToDetails(val restaurant: Restaurant) : RestaurantDiscoveryIntent
    data class SearchQueryChanged(val query: String) : RestaurantDiscoveryIntent
    data class ToggleOpenNow(val isOpenNow: Boolean) : RestaurantDiscoveryIntent
    data class ToggleFavourite(val restaurant: Restaurant) : RestaurantDiscoveryIntent
    data object LoadMore : RestaurantDiscoveryIntent
    data object Retry : RestaurantDiscoveryIntent
}

data class RestaurantDiscoveryFilterState(
    val query: String,
    val isOpenNow: Boolean,
    val totalCount: Int
)

sealed interface RestaurantDiscoveryUiState {
    data object Loading : RestaurantDiscoveryUiState
    data object Empty : RestaurantDiscoveryUiState

    data class Success(
        val restaurants: List<Restaurant>,
        val favourites: Set<String> = emptySet(),
        val isLoadingMore: Boolean = false
    ) : RestaurantDiscoveryUiState

    data class Error(
        val message: String,
    ) : RestaurantDiscoveryUiState
}
