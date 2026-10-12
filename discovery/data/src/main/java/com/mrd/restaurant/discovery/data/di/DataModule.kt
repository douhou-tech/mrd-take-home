package com.mrd.restaurant.discovery.data.di

import androidx.room.Room
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.core.logging.Logger
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSourceImpl
import com.mrd.restaurant.discovery.data.datasource.MockRemoteRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.RemoteRestaurantsDataSource
import com.mrd.restaurant.discovery.data.persistence.RestaurantDiscoveryDatabase
import com.mrd.restaurant.discovery.data.repository.RestaurantsRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val restaurantDiscoveryDataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            RestaurantDiscoveryDatabase::class.java,
            "restaurant_discovery.db",
        ).build()
    }
    single { get<RestaurantDiscoveryDatabase>().restaurantDao() }
    single<LocalRestaurantsDataSource> { LocalRestaurantsDataSourceImpl(restaurantDao = get()) }
    single<RemoteRestaurantsDataSource> {
        val logger: Logger = get()
        val restaurantsJson = try {
            androidContext().assets.open("mocks/restaurants.json")
                .bufferedReader()
                .use {
                    it.readText()
                }
        } catch (e: Exception) {
            logger.error("Failed to get restaurants mock json data")
            "[]"
        }
        MockRemoteRestaurantsDataSource(restaurantsJson)
    }
    single<RestaurantsRepository> {
        RestaurantsRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get(),
        )
    }
}
