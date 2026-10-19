package com.mrd.restaurant.discovery.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantRemoteModel(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("location") val location: String = "",
    @SerialName("cuisines") val cuisines: String = "",
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("delivery_fee_in_cents") val deliveryFeeInCents: Double = 0.0,
    @SerialName("eta_minutes") val etaMinutes: Int = 0,
    @SerialName("is_open") val isOpen: Boolean = false,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("favourite") val favourite: Boolean = false,
)
