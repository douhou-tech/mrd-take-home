package com.mrd.restaurant.discovery.feature.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mrd.restaurant.diiscovery.domain.model.Restaurant
import org.koin.androidx.compose.koinViewModel

@Composable
fun RestaurantDiscoveryScreen(
    viewModel: RestaurantDiscoveryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.SpacingMedium)
        ) {
            RestaurantSearchBox(
                query = uiState.query,
                onQueryChanged = {
                    viewModel.onIntent(RestaurantDiscoveryIntent.SearchQueryChanged(it))
                }
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

            RestaurantFilterSection(
                isOpenNow = uiState.isOpenNow,
                onToggleOpenNow = {
                    viewModel.onIntent(RestaurantDiscoveryIntent.ToggleOpenNow(it))
                }
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

            when (val state = uiState) {
                is RestaurantDiscoveryUiState.Loading -> LoadingResultsContent()
                is RestaurantDiscoveryUiState.Empty -> EmptyResultsContent()
                is RestaurantDiscoveryUiState.Error -> ErrorResultsContent(message = state.message)
                is RestaurantDiscoveryUiState.Success -> SuccessResultsContent(
                    state = state,
                    onPreviousPage = { viewModel.onIntent(RestaurantDiscoveryIntent.PreviousPage) },
                    onNextPage = { viewModel.onIntent(RestaurantDiscoveryIntent.NextPage) }
                )
            }
        }
    }
}

@Composable
private fun RestaurantSearchBox(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        label = { Text("Search restaurants...") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun RestaurantFilterSection(
    isOpenNow: Boolean,
    onToggleOpenNow: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Open Now", modifier = Modifier.weight(1f))
        Switch(
            checked = isOpenNow,
            onCheckedChange = onToggleOpenNow
        )
    }
}

@Composable
private fun EmptyResultsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No restaurants found.")
    }
}

@Composable
private fun LoadingResultsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorResultsContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Error: $message")
    }
}

@Composable
private fun SuccessResultsContent(
    state: RestaurantDiscoveryUiState.Success,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.restaurants, key = { it.id }) { restaurant ->
                RestaurantItem(
                    restaurant = restaurant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onPreviousPage,
                enabled = state.currentPage > 0
            ) {
                Text("Previous")
            }
            Spacer(modifier = Modifier.weight(1f))
            val totalPages = ((state.totalCount + state.pageSize - 1) / state.pageSize)
            Text("Page ${state.currentPage + 1} of $totalPages")
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onNextPage,
                enabled = (state.currentPage + 1) * state.pageSize < state.totalCount
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun RestaurantItem(restaurant: Restaurant) {

    Column(modifier = Modifier.padding(vertical = Dimens.SpacingSmall)) {
        RestaurantImage(
            imageUrl = restaurant.imageUrl
        )
        Text(text = restaurant.displayName(), style = MaterialTheme.typography.titleMedium)
        Text(text = restaurant.cuisines, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "Rating: ${restaurant.rating} ⭐ | ETA: ${restaurant.etaMinutes} mins",
            style = MaterialTheme.typography.bodySmall
        )
    }

}

@Composable
fun RestaurantImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .shimmerEffect()
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .height(150.dp)
    )
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerAnim"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = translateAnim.value)
        )
    )
}