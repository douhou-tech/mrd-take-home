package com.mrd.restaurant.discovery.data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrd.restaurant.discovery.data.persistence.dao.RestaurantDao
import com.mrd.restaurant.discovery.data.persistence.model.RestaurantEntity

@Database(entities = [RestaurantEntity::class], version = 1)
abstract class RestaurantDiscoveryDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao

}
