package com.mrd.restaurant.discovery.feature.screen.browse

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.feature.ui.Dimens
import com.mrd.restaurant.discovery.feature.component.MrDSearchBar
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantDiscoveryScreen(
    viewModel: RestaurantDiscoveryViewModel = koinViewModel(),
    navigator: FeatureNavigator,
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    val isKeyboardVisible = WindowInsets.isImeVisible

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            RestaurantSearchBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = Dimens.SpacingMedium, vertical = Dimens.SpacingSmall),
                query = filterState.query,
                onQueryChanged = {
                    viewModel.onIntent(RestaurantDiscoveryIntent.SearchQueryChanged(it))
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = Dimens.SpacingMedium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                RestaurantFilterSection(
                    isOpenNow = filterState.isOpenNow,
                    totalCount = filterState.totalCount,
                    onToggleOpenNow = {
                        viewModel.onIntent(RestaurantDiscoveryIntent.ToggleOpenNow(it))
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is RestaurantDiscoveryUiState.Empty -> EmptyResultsContent()
                        is RestaurantDiscoveryUiState.Error -> ErrorResultsContent(message = state.message)
                        is RestaurantDiscoveryUiState.Success -> SuccessResultsContent(
                            state = state,
                            onIntent = viewModel::onIntent,
                            onNavigateTo = navigator::navigateTo
                        )
                        else -> {}
                    }
                }
            }

            when {
                uiState is RestaurantDiscoveryUiState.Loading -> LoadingResultsContent()
                isKeyboardVisible -> KeyboardVisibleContentMask()
                else -> {}
            }
        }
    }
}


@Composable
private fun RestaurantSearchBar(
    modifier: Modifier,
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    MrDSearchBar(
        modifier = modifier,
        textFieldState = rememberTextFieldState(initialText = query),
        onSearch = {
            onQueryChanged(it)
        }
    )
}

@Composable
private fun RestaurantFilterSection(
    isOpenNow: Boolean,
    totalCount: Int,
    onToggleOpenNow: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        FilterChip(
            selected = isOpenNow,
            onClick = { onToggleOpenNow(!isOpenNow) },
            label = { Text("Open Now") }
        )
        Spacer(modifier = Modifier.weight(1f))
        Text("$totalCount results found")
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
    modifier: Modifier = Modifier,
    state: RestaurantDiscoveryUiState.Success,
    onIntent: (RestaurantDiscoveryIntent) -> Unit,
    onNavigateTo: (FeatureNavKey) -> Unit
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            Log.d("RestaurantDiscoveryScreen", "lastVisibleItemIndex: $lastVisibleItemIndex, totalItemsCount: $totalItemsCount")
            lastVisibleItemIndex >= totalItemsCount - 3 && totalItemsCount > 0 && !state.isLoadingMore

        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onIntent(RestaurantDiscoveryIntent.LoadMore)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
            val restaurants = state.restaurants
            items(restaurants.size, key = { restaurants[it].id }) { index ->
                val restaurant = state.restaurants[index]
                RestaurantItem(
                    modifier = Modifier.clickable {
                        onNavigateTo(FeatureNavKey.RestaurantDetails)
                    },
                    restaurant = restaurant
                )
                if (index != restaurants.lastIndex) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                }
            }
            if (state.isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpacingMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun RestaurantItem(
    modifier: Modifier = Modifier,
    restaurant: Restaurant
) {
    Column(modifier = modifier
        .padding(vertical = Dimens.SpacingSmall)
    ) {
        RestaurantImage(
            modifier = Modifier.padding(bottom = Dimens.SpacingSmall),
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
private fun RestaurantImage(
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

private fun Modifier.shimmerEffect(): Modifier = composed {
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

@Composable
private fun KeyboardVisibleContentMask() {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    )
}