package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository

class FavouriteRestaurantUseCase(
    private val repository: RestaurantsRepository,
    private val logger: Logger
) {

    suspend operator fun invoke(restaurantId: String, favourite: Boolean): EitherResult<Unit, Error> {
        return try {
            logger.log("Set favourite value to: $favourite, for restaurant: $restaurantId")
            repository.setFavourite(restaurantId, favourite)
            logger.log("Successfully updated favourite value to: $favourite, for restaurant: $restaurantId")
            EitherResult.Success(Unit)
        } catch (ex: Exception) {
            logger.error("Update restaurant: $restaurantId, favourite value failed", ex)
            EitherResult.Error(Error.Unknown)
        }
    }

    sealed interface Error {
        data object Unknown : Error
    }

}
