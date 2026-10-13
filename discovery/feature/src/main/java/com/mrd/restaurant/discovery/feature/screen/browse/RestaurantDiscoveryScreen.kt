package com.mrd.restaurant.discovery.feature.screen.browse

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavKey
import com.mrd.restaurant.discovery.feature.navigation.FeatureNavigator
import com.mrd.restaurant.discovery.feature.shared.component.KeyboardVisibleContentMask
import com.mrd.restaurant.discovery.feature.shared.component.MrDScreenLoader
import com.mrd.restaurant.discovery.feature.shared.component.MrDSearchBar
import com.mrd.restaurant.discovery.feature.shared.component.RemoteImage
import com.mrd.restaurant.discovery.feature.shared.container.MrDScreenScaffold
import com.mrd.restaurant.discovery.feature.ui.Dimens
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

    MrDScreenScaffold(
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

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(top = Dimens.SpacingMedium)
            ) {
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
            uiState is RestaurantDiscoveryUiState.Loading -> MrDScreenLoader()
            isKeyboardVisible -> KeyboardVisibleContentMask()
            else -> {}
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
                        onNavigateTo(FeatureNavKey.RestaurantDetails(restaurant.id))
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
        RemoteImage(
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
