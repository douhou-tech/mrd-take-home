package com.mrd.restaurant.discovery.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val id: String,
    val restaurantId: String,
    val category: String,
    val name: String,
    val description: String,
    val priceInCents: Int,
    val percentageOff: Int? = null,
    val unavailable: Boolean = false
)
