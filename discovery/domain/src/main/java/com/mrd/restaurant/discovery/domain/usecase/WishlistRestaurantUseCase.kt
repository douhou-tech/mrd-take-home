package com.mrd.restaurant.discovery.domain.usecase

import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.core.primitive.model.EitherResult
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository

class WishlistRestaurantUseCase(
    private val repository: RestaurantsRepository,
    private val logger: Logger
) {

    suspend operator fun invoke(restaurantId: String, wishlisted: Boolean): EitherResult<Unit, Error> {
        return try {
            logger.log("Set wishlisted value to: $wishlisted, for restaurant: $restaurantId")
            repository.setWishlisted(restaurantId, wishlisted)
            logger.log("Successfully updated wishlisted value to: $wishlisted, for restaurant: $restaurantId")
            EitherResult.Success(Unit)
        } catch (ex: Exception) {
            logger.error("Update restaurant: $restaurantId, wishlist value failed", ex)
            EitherResult.Error(Error.Unknown)
        }
    }

    sealed interface Error {
        data object Unknown : Error
    }

}