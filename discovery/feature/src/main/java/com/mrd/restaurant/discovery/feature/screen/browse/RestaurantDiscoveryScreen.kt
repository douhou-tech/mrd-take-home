package com.mrd.restaurant.discovery.feature.screen.browse

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mrd.restaurant.discovery.feature.R
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.feature.ui.component.RestaurantClosedOverlay
import com.mrd.restaurant.discovery.ui.Dimens
import com.mrd.restaurant.discovery.ui.component.FavouriteIconButton
import com.mrd.restaurant.discovery.ui.component.KeyboardVisibleContentMask
import com.mrd.restaurant.discovery.ui.component.MrDScreenLoader
import com.mrd.restaurant.discovery.ui.component.RemoteImage
import com.mrd.restaurant.discovery.ui.container.MrDScreenScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import com.mrd.restaurant.discovery.ui.theme.SearchBackground
import com.mrd.restaurant.discovery.ui.theme.MrDLightBlue
import kotlin.time.Duration.Companion.milliseconds

private const val LOAD_MORE_THRESHOLD = 5

@Composable
fun RestaurantDiscoveryScreen(
    viewModel: RestaurantDiscoveryViewModel,
    sharedTransitionScope: SharedTransitionScope,
    visibilityScope: AnimatedContentScope
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val state = uiState
            if (state !is RestaurantDiscoveryUiState.Success) return@derivedStateOf false
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            when {
                totalItemsCount <= 0 -> false
                lastVisibleItemIndex + LOAD_MORE_THRESHOLD >= totalItemsCount -> !state.isLoadingMore
                else -> false
            }
        }
    }
    var previousHideFilters by remember { mutableStateOf(false) }
    val shouldShowFilters by remember {
        derivedStateOf {
            if (listState.isScrollInProgress) {
                when {
                    listState.lastScrolledForward -> true
                    else -> false
                }
            } else {
                previousHideFilters
            }
        }
    }
    previousHideFilters = shouldShowFilters

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.onIntent(RestaurantDiscoveryIntent.LoadMore)
    }

    MrDScreenScaffold(
        topBar = {
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Dimens.SpacingMedium)
                .fillMaxSize()
                .clipToBounds()
        ) {
            AnimatedVisibility(!shouldShowFilters) {
                RestaurantFilterSection(
                    isOpenNow = filterState.isOpenNow,
                    totalCount = filterState.totalCount,
                    onToggleOpenNow = {
                        viewModel.onIntent(RestaurantDiscoveryIntent.ToggleOpenNow(it))
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is RestaurantDiscoveryUiState.Empty -> EmptyResultsContent()
                    is RestaurantDiscoveryUiState.Error -> ErrorResultsContent(message = state.message)
                    is RestaurantDiscoveryUiState.Success -> RestaurantDiscoveryContent(
                        listState = listState,
                        sharedTransitionScope = sharedTransitionScope,
                        visibilityScope = visibilityScope,
                        state = state,
                        onIntent = viewModel::onIntent
                    )

                    else -> {}
                }
            }
        }

        when {
            uiState is RestaurantDiscoveryUiState.Loading -> MrDScreenLoader()
            else -> {}
        }

        RestaurantSearchContent(
            query = filterState.query,
            showSearch = !shouldShowFilters,
            onQueryChanged = {
                viewModel.onIntent(RestaurantDiscoveryIntent.SearchQueryChanged(it))
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RestaurantSearchContent(
    query: String,
    showSearch: Boolean,
    onQueryChanged: (String) -> Unit
) {
    val state = rememberTextFieldState(initialText = query)
    val isKeyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .collectLatest {
                onQueryChanged(it)
            }
    }

    val width by animateFloatAsState(
        animationSpec = tween(200),
        targetValue = if (isKeyboardVisible) 0.9f else 0.7f,
        label = "cornerAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.BottomCenter,
    ) {
        KeyboardVisibleContentMask()

        AnimatedVisibility(
            showSearch,
            Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically {
                fullHeight -> fullHeight
            } + fadeIn(),
            exit = slideOutVertically {
                fullHeight -> fullHeight
            } + fadeOut()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .imePadding()
                    .padding(vertical = Dimens.SpacingSmall)
                    .clip(RoundedCornerShape(32.dp))
                    .background(SearchBackground)
                    .fillMaxWidth(width),
                state = state,
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MrDLightBlue,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.DarkGray,
                    unfocusedTextColor = Color.DarkGray,
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.DarkGray
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.restaurant_discovery_search_placeholder),
                        color = Color.DarkGray
                    )
                },
                lineLimits = TextFieldLineLimits.SingleLine
            )
        }
    }
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
            label = { Text(stringResource(R.string.restaurant_discovery_open_now)) }
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(stringResource(R.string.restaurant_discovery_results_found, totalCount))
    }
}

@Composable
private fun EmptyResultsContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.restaurant_discovery_no_results))
    }
}

@Composable
private fun ErrorResultsContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.restaurant_discovery_error, message))
    }
}

@Composable
private fun RestaurantDiscoveryContent(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    sharedTransitionScope: SharedTransitionScope,
    visibilityScope: AnimatedContentScope,
    state: RestaurantDiscoveryUiState.Success,
    onIntent: (RestaurantDiscoveryIntent) -> Unit,
) {
    var initialLoad by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1000L.milliseconds)
        initialLoad = false
    }
    val isTransitionRunning = visibilityScope.transition.isRunning || initialLoad

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState
    ) {
        val restaurants = state.restaurants
        items(restaurants.size, key = { restaurants[it].id }) { index ->
            val restaurant = state.restaurants[index]
            RestaurantItem(
                sharedTransitionScope = sharedTransitionScope,
                visibilityScope = visibilityScope,
                restaurant = restaurant,
                isTransitionRunning = isTransitionRunning,
                state = state,
                onIntent = onIntent
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

@Composable
private fun RestaurantItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    visibilityScope: AnimatedContentScope,
    restaurant: Restaurant,
    isTransitionRunning: Boolean,
    state: RestaurantDiscoveryUiState.Success,
    onIntent: (RestaurantDiscoveryIntent) -> Unit
) {
    val favourite = state.favourites.contains(restaurant.id)
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = modifier
                .clickable {
                    onIntent(RestaurantDiscoveryIntent.NavigateToDetails(restaurant))
                }
                .padding(vertical = Dimens.SpacingSmall)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                with (sharedTransitionScope) {
                    RemoteImage(
                        modifier = Modifier
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = restaurant.id),
                                animatedVisibilityScope = visibilityScope
                            )
                            .clip(
                                RoundedCornerShape(
                                    topStart = Dimens.SpacingSmall,
                                    topEnd = Dimens.SpacingSmall,
                                )
                            )
                            .padding(bottom = Dimens.SpacingSmall),
                        imageUrl = restaurant.imageUrl
                    )
                }

                RestaurantClosedOverlay(
                    isOpen = restaurant.isOpen
                )
            }
            Text(text = restaurant.displayName(), style = MaterialTheme.typography.titleMedium)
            Text(text = restaurant.cuisines, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.restaurant_discovery_rating_eta, restaurant.rating, restaurant.etaMinutes),
                style = MaterialTheme.typography.bodySmall
            )
        }

        AnimatedVisibility(
            visible = !isTransitionRunning,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = Dimens.SpacingSmall, end = Dimens.SpacingSmall)
        ) {
            FavouriteIconButton(
                favourite = favourite,
                onClick = {
                    onIntent(RestaurantDiscoveryIntent.ToggleFavourite(restaurant))
                }
            )
        }
    }
}
