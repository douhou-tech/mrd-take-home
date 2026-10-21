package com.mrd.restaurant.discovery.data.di

import androidx.room.Room
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSourceImpl
import com.mrd.restaurant.discovery.data.datasource.MockRemoteRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.RemoteRestaurantsDataSource
import com.mrd.restaurant.discovery.data.persistence.RestaurantDiscoveryDatabase
import com.mrd.restaurant.discovery.data.repository.RestaurantsRepositoryImpl
import com.mrd.restaurant.discovery.domain.repository.RestaurantsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val restaurantDiscoveryDataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            RestaurantDiscoveryDatabase::class.java,
            "restaurant_discovery.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true) // TODO remove for prod, instead use migrations
            .build()
    }
    single { get<RestaurantDiscoveryDatabase>().restaurantDao() }
    single<LocalRestaurantsDataSource> {
        LocalRestaurantsDataSourceImpl(
            restaurantDao = get()
        )
    }
    single<String>(named(DataModuleQualifiers.RESTAURANTS_JSON_MOCK)) {
        try {
            androidContext().assets.open("mocks/restaurants.json")
                .bufferedReader()
                .use {
                    it.readText()
                }
        } catch (e: Exception) {
            logger.error("Failed to get restaurants mock json data")
            "[]"
        }
    }
    single<String>(named(DataModuleQualifiers.RESTAURANTS_DETAILS_JSON_MOCK)) {
        try {
            androidContext().assets.open("mocks/restaurant_details.json")
                .bufferedReader()
                .use {
                    it.readText()
                }
        } catch (e: Exception) {
            logger.error("Failed to get restaurant details mock json data")
            "[]"
        }
    }
    single<RemoteRestaurantsDataSource> {
        val restaurantsJson = get<String>(named(DataModuleQualifiers.RESTAURANTS_JSON_MOCK))
        val restaurantDetailsJson = get<String>(named(DataModuleQualifiers.RESTAURANTS_DETAILS_JSON_MOCK))
        MockRemoteRestaurantsDataSource(restaurantsJson, restaurantDetailsJson)
    }
    single<RestaurantsRepository> {
        RestaurantsRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get(),
        )
    }
}

object DataModuleQualifiers {
    const val RESTAURANTS_JSON_MOCK = "restaurants_json_mock"
    const val RESTAURANTS_DETAILS_JSON_MOCK = "restaurants_details_json_mock"
}