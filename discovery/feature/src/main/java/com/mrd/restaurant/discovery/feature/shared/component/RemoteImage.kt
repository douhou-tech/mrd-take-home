package com.mrd.restaurant.discovery.feature.shared.component

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mrd.restaurant.discovery.feature.shared.shimmerEffect

@Composable
internal fun RemoteImage(
    modifier: Modifier = Modifier,
    imageUrl: String
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .shimmerEffect()
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .height(150.dp)
    )
}
