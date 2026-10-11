package com.mrd.restaurant.discovery.data.mapper

import com.mrd.restaurant.diiscovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.data.persistence.model.RestaurantEntity
import com.mrd.restaurant.discovery.data.remote.model.RestaurantRemoteModel


internal object RestaurantsMapper {
    fun RestaurantRemoteModel.toDomain(): Restaurant {
        return Restaurant(
            id = id,
            name = name,
            cuisines = cuisines,
            rating = rating,
            deliveryFeeInCents = deliveryFeeInCents,
            etaMinutes = etaMinutes,
            isOpen = isOpen,
            imageUrl = imageUrl
        )
    }

    fun RestaurantRemoteModel.toEntity(): RestaurantEntity {
        return RestaurantEntity(
            id = id,
            name = name,
            cuisines = cuisines,
            rating = rating,
            deliveryFeeInCents = deliveryFeeInCents,
            etaMinutes = etaMinutes,
            isOpen = isOpen,
            imageUrl = imageUrl
        )
    }

    fun RestaurantEntity.toDomain(): Restaurant {
        return Restaurant(
            id = id,
            name = name,
            cuisines = cuisines,
            rating = rating,
            deliveryFeeInCents = deliveryFeeInCents,
            etaMinutes = etaMinutes,
            isOpen = isOpen,
            imageUrl = imageUrl
        )
    }
}
