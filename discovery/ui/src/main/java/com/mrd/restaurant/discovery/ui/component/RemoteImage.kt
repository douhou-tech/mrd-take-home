package com.mrd.restaurant.discovery.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mrd.restaurant.discovery.ui.shimmerEffect

@Composable
fun RemoteImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
    @DrawableRes fallbackImage: Int,
) {
    val request = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .build()
    AsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        error = painterResource(fallbackImage),
        modifier = modifier
            .shimmerEffect()
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .height(150.dp)
    )
}
