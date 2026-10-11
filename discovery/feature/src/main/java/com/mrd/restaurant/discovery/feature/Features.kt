package com.mrd.restaurant.discovery.feature

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.util.DebugLogger
import okio.Path.Companion.toOkioPath


object Features {

    object RestaurantDiscovery {
        data class Config(
            val imageLoading: ImageLoading,
        ) {
            class Builder {
                private var imageLoading = ImageLoading()

                fun imageLoading(value: ImageLoading.() -> Unit) {
                    imageLoading = ImageLoading().apply(value)
                }

                fun build() = Config(imageLoading)
            }

            data class ImageLoading(
                val cacheDir: String = "restaurant_image_cache",
                val memoryCachePercent: Double = 0.25,
                val diskCacheSizeBytes: Long = 512L * 1024 * 1024,
                val enableDebugLogs: Boolean = false
            )
        }

        fun init(configure: (Config.Builder.() -> Unit)? = null) {
            val builder = Config.Builder()
            configure?.invoke(builder)
            val config = builder.build()

            SingletonImageLoader.setSafe { ctx ->
                with(config.imageLoading) {
                    ImageLoader.Builder(ctx)
                        .memoryCache {
                            MemoryCache.Builder()
                                .maxSizePercent(ctx, memoryCachePercent)
                                .build()
                        }
                        .diskCache {
                            DiskCache.Builder()
                                .directory(ctx.cacheDir.resolve(cacheDir).toOkioPath())
                                .maxSizeBytes(diskCacheSizeBytes)
                                .build()
                        }.apply {
                            if (enableDebugLogs) { logger(DebugLogger()) }
                        }
                        .build()
                }
            }
        }
    }

}
