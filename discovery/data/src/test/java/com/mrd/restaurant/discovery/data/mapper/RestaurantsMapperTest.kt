package com.mrd.restaurant.discovery.data.mapper

import com.mrd.restaurant.discovery.data.mapper.RestaurantsMapper.toDomain
import com.mrd.restaurant.discovery.data.remote.model.RestaurantRemoteModel
import org.junit.Test
import kotlin.test.assertEquals

class RestaurantsMapperTest {

    @Test
    fun `when mapping RestaurantRemoteModel toDomain then map correctly`() {
        val remote = RestaurantRemoteModel(
            id = "cpt-cbd-001",
            name = "Eastern Food Bazaar",
            cuisines = "Indian, Asian",
            rating = 4.5,
            deliveryFeeInCents = 1500,
            etaMinutes = 25,
            isOpen = true,
            imageUrl = "https://images.unsplash.com"
        )
        val domain = remote.toDomain()
        assertEquals(remote.id, domain.id)
        assertEquals(remote.name, domain.name)
        assertEquals(remote.cuisines, domain.cuisines)
        assertEquals(remote.rating, domain.rating)
        assertEquals(remote.deliveryFeeInCents, domain.deliveryFeeInCents)
        assertEquals(remote.etaMinutes, domain.etaMinutes)
        assertEquals(remote.isOpen, domain.isOpen)
        assertEquals(remote.imageUrl, domain.imageUrl)
    }


}