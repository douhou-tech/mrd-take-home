package com.mrd.restaurant.discovery.data.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MenuItem")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val category: String,
    val name: String,
    val description: String,
    val priceInCents: Int,
    val percentageOff: Int?,
    val unavailable: Boolean
)
