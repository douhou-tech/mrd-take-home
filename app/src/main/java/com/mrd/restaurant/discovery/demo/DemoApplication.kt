package com.mrd.restaurant.discovery.demo

import android.app.Application
import com.mrd.restaurant.diiscovery.domain.di.restaurantDiscoveryDomainModule
import com.mrd.restaurant.discovery.data.di.restaurantDiscoveryDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DemoApplication)
            modules(
                restaurantDiscoveryDomainModule,
                restaurantDiscoveryDataModule,
            )
        }
    }

}