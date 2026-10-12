package com.mrd.restaurant.discovery.domain.model

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val cuisines: String = "",
    val rating: Double = 0.0,
    val deliveryFeeInCents: Double = 0.0,
    val etaMinutes: Int = 0,
    val isOpen: Boolean = false,
    val imageUrl: String = ""
) {

    fun displayName() = "$name ($location)"

}
