package com.mrd.restaurant.discovery.demo

import android.app.Application
import com.mrd.restaurant.discovery.domain.restaurantDiscoveryDomainModule
import com.mrd.restaurant.discovery.data.di.restaurantDiscoveryDataModule
import com.mrd.restaurant.discovery.demo.di.appModule
import com.mrd.restaurant.discovery.feature.Features
import com.mrd.restaurant.discovery.feature.di.restaurantDetailsFeatureModule
import com.mrd.restaurant.discovery.feature.di.restaurantDiscoveryFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DemoApplication)
            modules(
                appModule,
                restaurantDiscoveryDomainModule,
                restaurantDiscoveryDataModule,
                restaurantDiscoveryFeatureModule,
                restaurantDetailsFeatureModule,
            )
        }

        Features.RestaurantDiscovery.init {
            imageLoading {
                Features.RestaurantDiscovery.Config.ImageLoading(
                    memoryCachePercent = 0.15
                )
            }
        }
    }

}