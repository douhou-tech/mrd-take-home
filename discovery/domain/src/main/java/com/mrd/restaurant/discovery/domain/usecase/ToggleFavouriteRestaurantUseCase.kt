package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ToggleFavouriteRestaurantUseCase(
    private val repository: RestaurantsRepository,
    private val logger: Logger
) {

    suspend operator fun invoke(restaurantId: String): EitherResult<Unit, Error> {
        return withContext(Dispatchers.IO) {
            val restaurant = try {
                repository.getLocalRestaurant(restaurantId) ?: return@withContext EitherResult.Error(
                    Error.NotFound
                )
            } catch (ex: Exception) {
                logger.error("Get restaurant: $restaurantId failed", ex)
                return@withContext EitherResult.Error(Error.Unknown)
            }
            return@withContext try {
                logger.log("Toggle favourite for restaurant: $restaurantId")
                logger.log("Current favourite value: ${restaurant.favourite}")
                val favourite = !restaurant.favourite
                repository.setFavourite(restaurantId, favourite)
                logger.log("Successfully updated favourite value to: ${favourite}, for restaurant: $restaurantId")
                EitherResult.Success(Unit)
            } catch (ex: Exception) {
                logger.error("Update restaurant: $restaurantId, favourite value failed", ex)
                EitherResult.Error(Error.Unknown)
            }
        }
    }

    sealed interface Error {
        data object NotFound : Error
        data object Unknown : Error
    }

}
