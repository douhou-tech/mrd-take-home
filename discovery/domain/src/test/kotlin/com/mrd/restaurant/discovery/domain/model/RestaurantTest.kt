package com.mrd.restaurant.discovery.domain.model

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.Test
import kotlin.random.Random

class RestaurantTest {

    @Test
    fun `formattedDeliveryFee should format zero cents correctly`() {
        val restaurant = Restaurant(deliveryFeeInCents = 0)
        restaurant.formattedDeliveryFee() shouldBeEqual "R0.00"
    }

    @Test
    fun `formattedDeliveryFee should format single digit cents correctly`() {
        val restaurant = Restaurant(deliveryFeeInCents = 5)
        restaurant.formattedDeliveryFee() shouldBeEqual "R0.05"
    }

    @Test
    fun `formattedDeliveryFee should format double digit cents correctly`() {
        val restaurant = Restaurant(deliveryFeeInCents = 50)
        restaurant.formattedDeliveryFee() shouldBeEqual "R0.50"
    }

    @Test
    fun `formattedDeliveryFee should format exactly one unit correctly`() {
        val restaurant = Restaurant(deliveryFeeInCents = 100)
        restaurant.formattedDeliveryFee() shouldBeEqual "R1.00"
    }

    @Test
    fun `formattedDeliveryFee should format mixed units and cents correctly`() {
        val restaurant = Restaurant(deliveryFeeInCents = 150)
        restaurant.formattedDeliveryFee() shouldBeEqual "R1.50"
    }

    @Test
    fun `formattedDeliveryFee should format large numbers correctly`() {
        val restaurant = Restaurant(deliveryFeeInCents = 10050)
        restaurant.formattedDeliveryFee() shouldBeEqual "R100.50"
    }

    @Test
    fun `formattedDeliveryFee should handle negative values by treating them as zero`() {
        val restaurant = Restaurant(deliveryFeeInCents = Random.nextInt(Integer.MIN_VALUE, -1))
        restaurant.formattedDeliveryFee() shouldBeEqual "R0.00"
    }

    @Test
    fun `formattedDeliveryFee should support custom currency`() {
        val restaurant = Restaurant(deliveryFeeInCents = 150)
        restaurant.formattedDeliveryFee("$") shouldBeEqual "$1.50"
    }
}
