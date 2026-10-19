package com.mrd.restaurant.discovery.feature.favourite

import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.domain.usecase.FavouriteRestaurantUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

interface FavouriteRestaurantDelegate {

    val favourites: Flow<Set<String>>

    fun toggleFavourite(
        scope: CoroutineScope,
        restaurantId: String,
        favourite: Boolean
    )

    suspend fun toggleFavourite(
        restaurantId: String
    ): EitherResult<Unit, Error>

    sealed interface Error {
        data object Unknown : Error
    }
}

class FavouriteRestaurantDelegateImpl(
    private val favouriteRestaurantUseCase: FavouriteRestaurantUseCase,
    private val restaurantsRepository: RestaurantsRepository
) : FavouriteRestaurantDelegate {

    override val favourites: Flow<Set<String>> = restaurantsRepository.observeFavouriteIds()

    private val debounceJobs = ConcurrentHashMap<String, Job>()

    override fun toggleFavourite(
        scope: CoroutineScope,
        restaurantId: String,
        favourite: Boolean
    ) {
        val targetFavourite = !favourite
        scope.launch {
            restaurantsRepository.setFavourite(restaurantId, targetFavourite)
        }
        debounceJobs[restaurantId]?.cancel()
        debounceJobs[restaurantId] = scope.launch {
            delay(300L.milliseconds)
            when (favouriteRestaurantUseCase(restaurantId, targetFavourite)) {
                is EitherResult.Success -> Unit
                is EitherResult.Error -> {
                    restaurantsRepository.setFavourite(restaurantId, favourite)
                }
            }
        }
    }

    override suspend fun toggleFavourite(
        restaurantId: String
    ): EitherResult<Unit, FavouriteRestaurantDelegate.Error> = withContext(Dispatchers.IO) {
        val currentFavourites = favourites.first()
        val currentFavourite = currentFavourites.contains(restaurantId)
        val targetFavourite = !currentFavourite

        restaurantsRepository.setFavourite(restaurantId, targetFavourite)

        when (favouriteRestaurantUseCase(restaurantId, targetFavourite)) {
            is EitherResult.Success -> EitherResult.Success(Unit)
            is EitherResult.Error -> {
                restaurantsRepository.setFavourite(restaurantId, currentFavourite)
                EitherResult.Error(FavouriteRestaurantDelegate.Error.Unknown)
            }
        }
    }
}