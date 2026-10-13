package com.mrd.restaurant.discovery.feature.shared.container

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mrd.restaurant.discovery.feature.ui.Dimens

@Composable
internal fun MrDScreenScaffold(
    topBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { topBar() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpacingMedium)
        ) {
            content()
        }
    }
}