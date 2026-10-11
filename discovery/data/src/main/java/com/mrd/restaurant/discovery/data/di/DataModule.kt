package com.mrd.restaurant.discovery.data.di

import androidx.room.Room
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSourceImpl
import com.mrd.restaurant.discovery.data.datasource.MockRemoteDataSource
import com.mrd.restaurant.discovery.data.datasource.RemoteDataSource
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
    single<RemoteDataSource> {
        val restaurantsJson = androidContext().assets.open("mocks/restaurants.json")
            .bufferedReader()
            .use {
                it.readText()
            }
        MockRemoteDataSource(restaurantsJson)
    }
    single<RestaurantsRepository> { RestaurantsRepositoryImpl(localDataSource = get()) }
}
