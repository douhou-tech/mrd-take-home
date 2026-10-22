package com.mrd.restaurant.discovery.feature

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.util.DebugLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
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

        @OptIn(ExperimentalCoilApi::class)
        fun init(configure: (Config.Builder.() -> Unit)? = null) {
            val builder = Config.Builder()
            configure?.invoke(builder)
            val config = builder.build()

            val client = HttpClient {
                install(HttpTimeout) {
                    requestTimeoutMillis = 10_000L
                    connectTimeoutMillis = 10_000L
                    socketTimeoutMillis = 10_000L
                }
            }

            SingletonImageLoader.setSafe { ctx ->
                with(config.imageLoading) {
                    ImageLoader.Builder(ctx)
                        .components {
                            add(KtorNetworkFetcherFactory(httpClient = client))
                        }
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
