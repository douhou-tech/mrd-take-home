package com.mrd.restaurant.discovery.data.di

import androidx.room.Room
import com.mrd.restaurant.diiscovery.domain.repository.RestaurantsRepository
import com.mrd.restaurant.discovery.data.datasource.LocalRestaurantsDataSource
import com.mrd.restaurant.discovery.data.datasource.RestaurantDiscoveryDatabase
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
    single { LocalRestaurantsDataSource(restaurantDao = get()) }
    single<RestaurantsRepository> { RestaurantsRepositoryImpl(localDataSource = get()) }
}
