package com.mrd.restaurant.discovery.feature.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrd.restaurant.diiscovery.domain.model.Restaurant
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.diiscovery.domain.usecase.FilterRestaurantsUseCase
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantDiscoveryViewModel(
    private val filterRestaurantsUseCase: FilterRestaurantsUseCase,
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

    private val _uiState = MutableStateFlow<RestaurantDiscoveryUiState>(
        RestaurantDiscoveryUiState.Loading(query = query, isOpenNow = isOpenNow)
    )
    val uiState: StateFlow<RestaurantDiscoveryUiState> = _uiState.asStateFlow()

    init {
        loadRestaurants()
    }

    fun onIntent(intent: RestaurantDiscoveryIntent) {
        when (intent) {
            is RestaurantDiscoveryIntent.SearchQueryChanged -> {
                if (query != intent.query) {
                    query = intent.query
                    currentPage = 0
                    loadRestaurants()
                }
            }
            is RestaurantDiscoveryIntent.ToggleOpenNow -> {
                if (isOpenNow != intent.isOpenNow) {
                    isOpenNow = intent.isOpenNow
                    currentPage = 0
                    loadRestaurants()
                }
            }
            RestaurantDiscoveryIntent.NextPage -> {
                val currentState = _uiState.value
                if (currentState is RestaurantDiscoveryUiState.Success) {
                    if ((currentPage + 1) * pageSize < currentState.totalCount) {
                        currentPage++
                        loadRestaurants()
                    }
                }
            }
            RestaurantDiscoveryIntent.PreviousPage -> {
                if (currentPage > 0) {
                    currentPage--
                    loadRestaurants()
                }
            }
        }
    }

    private fun loadRestaurants() {
        val filtersList = mutableListOf<RestaurantFilter>()
        if (query.isNotBlank()) {
            filtersList.add(RestaurantFilter.Query(query))
        }
        if (isOpenNow) {
            filtersList.add(RestaurantFilter.IsOpenNow)
        }
        val filters = RestaurantFilters(filtersList.toSet())

        viewModelScope.launch {
            _uiState.value = RestaurantDiscoveryUiState.Loading(
                query = query,
                isOpenNow = isOpenNow
            )

            val pagination = Pagination(page = currentPage, pageSize = pageSize)
            when (val result = filterRestaurantsUseCase(filters, pagination)) {
                is EitherResult.Success -> {
                    val paged = result.data
                    if (paged.items.isEmpty()) {
                        _uiState.value = RestaurantDiscoveryUiState.Empty(
                            query = query,
                            isOpenNow = isOpenNow
                        )
                    } else {
                        _uiState.value = RestaurantDiscoveryUiState.Success(
                            query = query,
                            isOpenNow = isOpenNow,
                            restaurants = paged.items,
                            currentPage = paged.currentPage,
                            pageSize = paged.pageSize,
                            totalCount = paged.totalCount,
                        )
                    }
                }
                is EitherResult.Error -> {
                    _uiState.value = RestaurantDiscoveryUiState.Error(
                        query = query,
                        isOpenNow = isOpenNow,
                        message = "Failed to load restaurants"
                    )
                }
            }
        }
    }

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_IS_OPEN_NOW = "is_open_now"
        private const val KEY_CURRENT_PAGE = "current_page"
        private const val KEY_PAGE_SIZE = "page_size"
    }
}

sealed interface RestaurantDiscoveryIntent {
    data class SearchQueryChanged(val query: String) : RestaurantDiscoveryIntent
    data class ToggleOpenNow(val isOpenNow: Boolean) : RestaurantDiscoveryIntent
    data object NextPage : RestaurantDiscoveryIntent
    data object PreviousPage : RestaurantDiscoveryIntent
}

sealed interface RestaurantDiscoveryUiState {
    val query: String
    val isOpenNow: Boolean

    data class Loading(
        override val query: String = "",
        override val isOpenNow: Boolean = false,
    ) : RestaurantDiscoveryUiState

    data class Empty(
        override val query: String = "",
        override val isOpenNow: Boolean = false,
    ) : RestaurantDiscoveryUiState

    data class Success(
        override val query: String = "",
        override val isOpenNow: Boolean = false,
        val restaurants: List<Restaurant>,
        val currentPage: Int,
        val pageSize: Int,
        val totalCount: Int,
    ) : RestaurantDiscoveryUiState

    data class Error(
        override val query: String = "",
        override val isOpenNow: Boolean = false,
        val message: String,
    ) : RestaurantDiscoveryUiState
}
