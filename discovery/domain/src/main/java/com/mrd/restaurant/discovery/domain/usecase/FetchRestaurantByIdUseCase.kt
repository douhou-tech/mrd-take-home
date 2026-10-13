package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository

class FetchRestaurantByIdUseCase(
    private val repository: RestaurantsRepository,
    private val logger: Logger
) {

    suspend operator fun invoke(restaurantId: String): EitherResult<Restaurant, Error> {
        return try {
            logger.log("Fetching restaurant with id: $restaurantId")
            val restaurant = repository.getRestaurant(restaurantId)
            if (restaurant == null) {
                logger.log("Restaurant with id: $restaurantId not found")
                EitherResult.Error(Error.NotFound)
            } else {
                logger.log("Fetched restaurant: $restaurant")
                EitherResult.Success(restaurant)
            }
        } catch (ex: Exception) {
            logger.error("Error fetching restaurant with id: $restaurantId", ex)
            EitherResult.Error(Error.Unknown)
        }
    }

    sealed interface Error {
        data object Unknown : Error
        data object NotFound : Error
    }
}