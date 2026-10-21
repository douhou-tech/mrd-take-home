package com.mrd.restaurant.discovery.feature.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mrd.restaurant.discovery.ui.theme.Black60
import com.mrd.restaurant.discovery.feature.R

@Composable
fun BoxScope.RestaurantClosedOverlay(isOpen: Boolean) {
    if (!isOpen) {
        Box(
            modifier = Modifier
                .background(Black60)
                .matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(R.string.restaurant_closed)
            )
        }
    }
}