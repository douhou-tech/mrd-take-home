package com.mrd.restaurant.discovery.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WishlistIconButton(
    modifier: Modifier = Modifier,
    wishlisted: Boolean,
    onClick: () -> Unit
) {
    val icon = when (wishlisted) {
        true -> Icons.Default.Favorite
        else -> Icons.Default.FavoriteBorder
    }
    MrDIconButton(
        modifier = modifier,
        icon = icon,
        onClick = onClick
    )
}
