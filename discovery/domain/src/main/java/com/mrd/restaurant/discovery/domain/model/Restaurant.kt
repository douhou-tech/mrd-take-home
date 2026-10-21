package com.mrd.restaurant.discovery.domain.model

import kotlinx.serialization.Serializable

const val DEFAULT_CURRENCY = "R"

@Serializable
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val cuisines: String = "",
    val rating: Double = 0.0,
    val deliveryFeeInCents: Int = 0,
    val etaMinutes: Int = 0,
    val isOpen: Boolean = false,
    val imageUrl: String = "",
    val favourite: Boolean = false,
    val wishlisted: Boolean = favourite,
    val menuItems: List<MenuItem> = emptyList()
) {

    fun displayName() = "$name ($location)"

    fun formattedDeliveryFee(currency: String = DEFAULT_CURRENCY): String {
        val safeCents = kotlin.math.max(0, deliveryFeeInCents)
        return String.format(java.util.Locale.US, "%s%.2f", currency, safeCents / 100.0)
    }
}
