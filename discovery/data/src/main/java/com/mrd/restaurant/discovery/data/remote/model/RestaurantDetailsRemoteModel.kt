package com.mrd.restaurant.discovery.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantDetailsRemoteModel(
    val id: String,
    @SerialName("restaurant_info") val restaurantInfo: RestaurantRemoteModel,
    val menu: List<MenuCategoryRemoteModel>
)

@Serializable
data class MenuCategoryRemoteModel(
    val category: String,
    val items: List<MenuItemRemoteModel>
)

@Serializable
data class MenuItemRemoteModel(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("price_in_cents") val priceInCents: Int
)

@Serializable
data class RestaurantDetailsMetaRemoteModel(
    @SerialName("on_sale") val onSale: OnSaleRemoteModel,
    val unavailable: List<String>
)

@Serializable
data class OnSaleRemoteModel(
    @SerialName("item_ids") val itemIds: List<String>,
    @SerialName("percentage_off") val percentageOff: Int
)
