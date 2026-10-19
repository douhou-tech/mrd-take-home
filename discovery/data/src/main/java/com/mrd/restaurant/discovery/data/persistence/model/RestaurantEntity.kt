package com.mrd.restaurant.discovery.data.persistence.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Restaurant")
data class RestaurantEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val location: String = "",
    val cuisines: String = "",
    val rating: Double = 0.0,
    val deliveryFeeInCents: Double = 0.0,
    val etaMinutes: Int = 0,
    val isOpen: Boolean = false,
    val imageUrl: String = "",
    val favourite: Boolean = false
)

data class RestaurantEntityPagedResult(
    @Embedded val restaurant: RestaurantEntity,
    val totalCount: Int = 0
)
