package com.mrd.restaurant.discovery.feature.screen.details

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrd.restaurant.discovery.feature.R
import com.mrd.restaurant.discovery.ui.theme.Black40
import com.mrd.restaurant.discovery.domain.model.MenuItem
import com.mrd.restaurant.discovery.domain.model.Restaurant
import com.mrd.restaurant.discovery.feature.ui.component.RestaurantClosedOverlay
import com.mrd.restaurant.discovery.ui.Dimens
import com.mrd.restaurant.discovery.ui.component.FavouriteIconButton
import com.mrd.restaurant.discovery.ui.component.MrDIconButton
import com.mrd.restaurant.discovery.ui.component.MrDRetryContent
import com.mrd.restaurant.discovery.ui.component.MrDScreenLoader
import com.mrd.restaurant.discovery.ui.component.RemoteImage
import com.mrd.restaurant.discovery.ui.container.MrDScreenScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RestaurantDetailsScreen(
    viewModel: RestaurantDetailsViewModel,
    sharedTransitionScope: SharedTransitionScope,
    visibilityScope: AnimatedContentScope
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    var showDetails by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            var accumulatedScroll = 0f
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta > 0 && accumulatedScroll < 0) accumulatedScroll = 0f
                if (delta < 0 && accumulatedScroll > 0) accumulatedScroll = 0f
                
                accumulatedScroll += delta

                if (accumulatedScroll < -50f) {
                    showDetails = false
                    accumulatedScroll = 0f
                } else if (accumulatedScroll > 50f) {
                    showDetails = true
                    accumulatedScroll = 0f
                }
                return Offset.Zero
            }
        }
    }

    MrDScreenScaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) {
        when (val state = uiState) {
            is RestaurantDetailsUiState.Error -> {
                MrDRetryContent(
                    message = state.message,
                    onRetry = {
                        viewModel.onIntent(RestaurantDetailsIntent.Retry)
                    }
                )
            }
            RestaurantDetailsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RestaurantDetailsUiState.PartialDataState -> {
                RestaurantDetailsContent(
                    visibilityScope = visibilityScope,
                    sharedTransitionScope = sharedTransitionScope,
                    showDetails = showDetails,
                    restaurant = state.data.restaurant,
                    favourite = state.data.favourite,
                    onIntent = viewModel::onIntent,
                    menuContent = {
                        when (state) {
                            is RestaurantDetailsUiState.LoadingMenu -> {
                                RestaurantMenu(
                                    isLoadingMenu = true
                                )
                            }
                            is RestaurantDetailsUiState.MenuError -> RestaurantMenu(
                                isMenuError = true,
                                onIntent = viewModel::onIntent
                            )
                            is RestaurantDetailsUiState.Success -> {
                                RestaurantMenu(
                                    listState = listState,
                                    menuItems = state.data.menuItems,
                                    onIntent = viewModel::onIntent
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RestaurantDetailsContent(
    sharedTransitionScope: SharedTransitionScope,
    visibilityScope: AnimatedContentScope,
    showDetails: Boolean,
    restaurant: Restaurant,
    favourite: Boolean,
    onIntent: (RestaurantDetailsIntent) -> Unit,
    menuContent: @Composable ColumnScope.() -> Unit,
) {
    var isRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(850L.milliseconds)
        isRevealed = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {

        AnimatedVisibility(
            showDetails
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                stickyHeader {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GradientShadowBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(2f),
                            isRevealed = isRevealed,
                            overlayContent = {
                                AnimatedVisibility(
                                    visible = isRevealed,
                                    enter = fadeIn(animationSpec = tween(500)),
                                    exit = fadeOut(animationSpec = tween(300))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().zIndex(1f),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        MrDIconButton(
                                            icon = Icons.AutoMirrored.Default.ArrowBack,
                                            onClick = {
                                                onIntent(RestaurantDetailsIntent.Back)
                                            }
                                        )

                                        FavouriteIconButton(
                                            favourite = favourite,
                                            onClick = {
                                                onIntent(RestaurantDetailsIntent.ToggleFavourite)
                                            }
                                        )
                                    }
                                }
                            }
                        ) {
                            Box(modifier = Modifier.zIndex(0f)) {
                                with(sharedTransitionScope) {
                                    RemoteImage(
                                        modifier = Modifier.sharedElement(
                                            sharedTransitionScope.rememberSharedContentState(key = restaurant.id),
                                            animatedVisibilityScope = visibilityScope,
                                        ),
                                        imageUrl = restaurant.imageUrl
                                    )
                                }

                                RestaurantClosedOverlay(
                                    isOpen = restaurant.isOpen
                                )
                            }

                        }

                    }
                }


                item {
                    AnimatedVisibility(
                        visible = isRevealed,
                        modifier = Modifier.zIndex(1f),
                        enter = expandVertically(tween(300)) + fadeIn(),
                        exit = shrinkVertically(tween(300)) + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpacingMedium),
                        ) {
                            Text(
                                text = restaurant.name,
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                            )
                            Text(
                                text = restaurant.location,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                            DescriptionTextItem(
                                icon = Icons.Default.Star,
                                text = "${restaurant.rating}"
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                            DescriptionTextItem(
                                icon = Icons.Default.DateRange,
                                text = stringResource(R.string.restaurant_eta_mins, restaurant.etaMinutes)
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                            DescriptionTextItem(
                                icon = Icons.Default.ShoppingCart,
                                text = stringResource(R.string.restaurant_delivery_fee, restaurant.formattedDeliveryFee())
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                            Text(
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = restaurant.cuisines
                            )
                        }
                    }
                }

            }
        }


        menuContent()
    }

}

@Composable
private fun RestaurantMenu(
    listState: LazyListState = rememberLazyListState(),
    isLoadingMenu: Boolean = false,
    isMenuError: Boolean = false,
    menuItems: List<MenuItem>? = null,
    onIntent: (RestaurantDetailsIntent) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoadingMenu -> MrDScreenLoader()
            isMenuError -> {
                MrDRetryContent(
                    message = stringResource(R.string.error_failed_to_load_menu_items),
                    onRetry = {
                        onIntent(RestaurantDetailsIntent.Retry)
                    }
                )
            }
            menuItems.isNullOrEmpty() -> {
                MrDRetryContent(
                    message = stringResource(R.string.error_no_menu_items),
                    onRetry = {
                        onIntent(RestaurantDetailsIntent.Retry)
                    }
                )
            }
            else -> {
                val groupedItems = remember(menuItems) {
                    menuItems.groupBy { it.category }
                }
                val categories = remember(groupedItems) {
                    groupedItems.keys.toList()
                }
                val pagerState = rememberPagerState(pageCount = { categories.size })
                val coroutineScope = rememberCoroutineScope()

                Column(modifier = Modifier.fillMaxSize()) {
                    PrimaryScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 16.dp
                    ) {
                        categories.forEachIndexed { index, category ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = { Text(text = category) }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val category = categories[page]
                        val items = groupedItems[category] ?: emptyList()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState
                        ) {
                            items(items, key = { it.id }) { item ->
                                MenuItemRow(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(item: MenuItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpacingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium
            )
            val priceStr = String.format(java.util.Locale.US, "%.2f", item.priceInCents / 100.0)
            Text(
                text = stringResource(R.string.price_format, priceStr),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (item.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DescriptionTextItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = icon,
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(Dimens.SpacingMedium))
        Text(text = text)
    }
}

@Composable
private fun GradientShadowBox(
    modifier: Modifier = Modifier,
    shadowColor: Color = Black40,
    shadowHeight: Dp = 48.dp,
    isRevealed: Boolean = true,
    overlayContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        AnimatedVisibility(
            visible = isRevealed,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.matchParentSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(shadowHeight)
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(shadowColor, Color.Transparent)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(shadowHeight)
                        .align(Alignment.BottomCenter)
                        .zIndex(1f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, shadowColor)
                            )
                        )
                )
            }
        }

        overlayContent()
    }
}
