package com.mrd.restaurant.discovery.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val cuisines: String = "",
    val rating: Double = 0.0,
    val deliveryFeeInCents: Double = 0.0,
    val etaMinutes: Int = 0,
    val isOpen: Boolean = false,
    val imageUrl: String = "",
    val wishlisted: Boolean = false
) {

    fun displayName() = "$name ($location)"

}
