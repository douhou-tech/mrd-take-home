package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.model.MenuItem
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository

class FetchMenuItemsByRestaurantIdUseCase(
    private val repository: RestaurantsRepository,
    private val logger: Logger
) {

    suspend operator fun invoke(restaurantId: String): EitherResult<List<MenuItem>, Error> {
        return try {
            logger.log("Fetching menu items for restaurant with id: $restaurantId")
            val restaurant = repository.getRestaurant(restaurantId)
            if (restaurant != null) {
                logger.log("Fetched menu items for restaurant: $restaurantId")
                EitherResult.Success(restaurant.menuItems)
            } else {
                EitherResult.Error(Error.Unknown)
            }
        } catch (ex: Exception) {
            logger.error("Error fetching menu items for restaurant with id: $restaurantId", ex)
            EitherResult.Error(Error.Unknown)
        }
    }

    sealed interface Error {
        data object Unknown : Error
    }
}
