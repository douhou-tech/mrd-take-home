package com.mrd.restaurant.discovery.feature.favourite

import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.domain.usecase.ToggleFavouriteRestaurantUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

interface FavouriteRestaurantDelegate {

    val favourites: Flow<Set<String>>

    fun toggleFavourite(
        restaurantId: String
    )
}

class FavouriteRestaurantDelegateImpl(
    private val toggleFavouriteRestaurantUseCase: ToggleFavouriteRestaurantUseCase,
    restaurantsRepository: RestaurantsRepository
) : FavouriteRestaurantDelegate {

    private val scope = CoroutineScope(Dispatchers.IO)
    override val favourites: Flow<Set<String>> = restaurantsRepository.observeFavouriteIds()
    private val debounceJobs = ConcurrentHashMap<String, Job>()

    override fun toggleFavourite(
        restaurantId: String,
    ) {
        debounceJobs[restaurantId]?.cancel()
        debounceJobs[restaurantId] = scope.launch {
            delay(300L.milliseconds)
            toggleFavouriteRestaurantUseCase(restaurantId)
        }
    }

}