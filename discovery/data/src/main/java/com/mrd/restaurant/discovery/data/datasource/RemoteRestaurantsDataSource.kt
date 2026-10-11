package com.mrd.restaurant.discovery.data.datasource

import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilter
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantFilters
import com.mrd.restaurant.discovery.core.primitive.model.PagedResult
import com.mrd.restaurant.discovery.core.primitive.model.Pagination
import com.mrd.restaurant.discovery.data.persistence.model.RestaurantEntity
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

interface RemoteDataSource {
    suspend fun getRestaurants(
        filters: RestaurantFilters = RestaurantFilters(),
        pagination: Pagination = Pagination(page = 0, pageSize = 20),
    ): PagedResult<RestaurantEntity>
}

class MockRemoteDataSource : RemoteDataSource {

    private val capeTownCbdRestaurants: List<RestaurantEntity> = build100CapeTownRestaurants()

    override suspend fun getRestaurants(
        filters: RestaurantFilters,
        pagination: Pagination
    ): PagedResult<RestaurantEntity> {
        var result = capeTownCbdRestaurants

        filters.filters.forEach { filter ->
            when (filter) {
                is RestaurantFilter.Query -> {
                    val query = filter.query.trim().lowercase()
                    if (query.isNotEmpty()) {
                        result = result.filter {
                            it.name.lowercase().contains(query) ||
                                it.cuisines.lowercase().contains(query)
                        }
                    }
                }
                is RestaurantFilter.IsOpenNow -> {
                    result = result.filter { it.isOpen }
                }
            }
        }

        val totalCount = result.size
        val pageSize = if (pagination.pageSize > 0) pagination.pageSize else totalCount
        val startIndex = max(0, pagination.page * pageSize)
        val endIndex = min(totalCount, startIndex + pageSize)

        val items = if (startIndex < totalCount) {
            result.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        return PagedResult(
            items = items,
            currentPage = pagination.page,
            pageSize = pageSize,
            totalCount = totalCount
        )
    }
}

private data class BaseRestaurant(
    val name: String,
    val cuisines: String,
    val rating: Double,
    val deliveryFeeInCents: Double,
    val etaMinutes: Int,
    val isOpen: Boolean,
    val imageUrl: String,
)

private val baseRestaurants = listOf(
    BaseRestaurant("Eastern Food Bazaar", "Indian, Asian, Street Food", 4.5, 1500.0, 25, true, "https://images.unsplash.com/photo-1585937421612-70a008356fbe"),
    BaseRestaurant("Kloof Street House", "Mediterranean, Brasserie, Cocktails", 4.7, 2500.0, 35, true, "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4"),
    BaseRestaurant("Clarke's Bar & Dining Room", "Burgers, American, Breakfast", 4.6, 1800.0, 20, true, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd"),
    BaseRestaurant("Honest Chocolate Café", "Desserts, Bakery, Coffee", 4.8, 1200.0, 15, true, "https://images.unsplash.com/photo-1551024709-8f23befc6f87"),
    BaseRestaurant("Nü Health Food Café", "Healthy, Salads, Smoothies", 4.4, 2000.0, 25, true, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd"),
    BaseRestaurant("Belly of the Beast", "Fine Dining, Modern South African", 4.9, 3000.0, 40, false, "https://images.unsplash.com/photo-1544025162-d76694265947"),
    BaseRestaurant("South China Dim Sum Bar", "Asian, Dim Sum, Chinese", 4.6, 2200.0, 30, true, "https://images.unsplash.com/photo-1496116218417-1a781b1c416c"),
    BaseRestaurant("Truth Coffee Roasting", "Café, Coffee, Bakery", 4.7, 1500.0, 20, true, "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb"),
    BaseRestaurant("Burger & Lobster Cape Town", "Burgers, Seafood, American", 4.5, 2500.0, 30, true, "https://images.unsplash.com/photo-1550547660-d9450f859349"),
    BaseRestaurant("Tiger's Milk Bree Street", "Pub Food, Pizza, Burgers", 4.3, 2000.0, 25, true, "https://images.unsplash.com/photo-1513104890138-7c749659a591"),
    BaseRestaurant("House of H", "Grill, Meats, Salad Bar", 4.5, 2100.0, 28, true, "https://images.unsplash.com/photo-1555396273-367ea4eb4db5"),
    BaseRestaurant("Chefs Warehouse Bree Street", "Tapas, Modern, Fine Dining", 4.8, 3500.0, 35, true, "https://images.unsplash.com/photo-1559339352-11d035aa65de"),
    BaseRestaurant("Seabreeze Fish & Shell", "Seafood, Oysters, Grill", 4.6, 2400.0, 30, true, "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb"),
    BaseRestaurant("Black Sheep Restaurant", "Bistro, Seasonal, Wine Bar", 4.7, 2600.0, 32, true, "https://images.unsplash.com/photo-1550966871-3ed3cdb5ed0c"),
    BaseRestaurant("Maria's Greek Café", "Greek, Mediterranean, Tapas", 4.6, 1900.0, 25, true, "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe"),
    BaseRestaurant("Loading Bay", "Café, Burgers, Specialty Coffee", 4.5, 2200.0, 22, true, "https://images.unsplash.com/photo-1509042239860-f550ce710b93"),
    BaseRestaurant("Yours Truly Kloof", "Café, Pizza, Garden Bar", 4.4, 1800.0, 24, true, "https://images.unsplash.com/photo-1514933651103-005eec06c04b"),
    BaseRestaurant("Bao Down", "Asian Fusion, Bao Buns, Tapas", 4.7, 2300.0, 27, true, "https://images.unsplash.com/photo-1563245372-f21724e3856d"),
    BaseRestaurant("Jason Bakery", "Bakery, Pastries, Coffee", 4.8, 1400.0, 18, true, "https://images.unsplash.com/photo-1509440159596-0249088772ff"),
    BaseRestaurant("Fyn Restaurant", "Japanese-African, Fine Dining", 4.9, 4000.0, 45, false, "https://images.unsplash.com/photo-1578474846511-04ba529f0b88"),
    BaseRestaurant("Swan Café", "French, Crêperie, Café", 4.6, 1600.0, 20, true, "https://images.unsplash.com/photo-1504754524776-8f4f37790ca0"),
    BaseRestaurant("Addis in Cape", "Ethiopian, Traditional, Vegan Friendly", 4.7, 2000.0, 30, true, "https://images.unsplash.com/photo-1541544741938-0af808871cc0"),
    BaseRestaurant("The Fat Cactus", "Mexican, Margaritas, Tex-Mex", 4.3, 1900.0, 25, true, "https://images.unsplash.com/photo-1565299585323-38d6b0865b47"),
    BaseRestaurant("El Burro Taqueria", "Mexican, Tacos, Tequila Bar", 4.6, 2200.0, 28, true, "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b"),
    BaseRestaurant("Villa 47", "Italian, Tapas, Bistro", 4.5, 2500.0, 30, true, "https://images.unsplash.com/photo-1555396273-367ea4eb4db5")
)

private val locations = listOf(
    "Bree Street", "Long Street", "Kloof Street", "Loop Street", "Harrington Street",
    "De Waterkant", "Foreshore", "Bo-Kaap", "Strand Street", "Castle Street",
    "Waterkant Street", "Gardens", "Oranjezicht", "Tamboerskloof", "Green Point",
    "Sea Point", "V&A Waterfront", "Woodstock", "Observatory", "Salt River"
)

private fun build100CapeTownRestaurants(): List<RestaurantEntity> {
    val list = mutableListOf<RestaurantEntity>()
    var idCounter = 1
    for (i in 0 until 100) {
        val base = baseRestaurants[i % baseRestaurants.size]
        val location = locations[i % locations.size]
        val suffix = if (i >= baseRestaurants.size) " ($location)" else ""
        val idStr = String.format(Locale.US, "cpt-cbd-%03d", idCounter++)
        val ratingVariation = (((base.rating + ((i % 5) * 0.1)) - 0.2).coerceIn(3.8, 5.0) * 10).toInt() / 10.0
        val isOpen = (i % 7 != 5) && base.isOpen
        val fee = (1200 + (i * 130) % 2500).toDouble()
        val eta = 15 + (i * 3) % 30

        list.add(
            RestaurantEntity(
                id = idStr,
                name = "${base.name}$suffix",
                cuisines = base.cuisines,
                rating = ratingVariation,
                deliveryFeeInCents = fee,
                etaMinutes = eta,
                isOpen = isOpen,
                imageUrl = base.imageUrl,
            )
        )
    }
    return list
}
