package com.mrd.restaurant.discovery.feature.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = {
                    viewModel.onIntent(RestaurantDiscoveryIntent.SearchQueryChanged(it))
                },
                label = { Text("Search restaurants...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Now", modifier = Modifier.weight(1f))
                Switch(
                    checked = uiState.isOpenNow,
                    onCheckedChange = {
                        viewModel.onIntent(RestaurantDiscoveryIntent.ToggleOpenNow(it))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is RestaurantDiscoveryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RestaurantDiscoveryUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No restaurants found.")
                    }
                }
                is RestaurantDiscoveryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}")
                    }
                }
                is RestaurantDiscoveryUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(state.restaurants, key = { it.id }) { restaurant ->
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(text = restaurant.name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = restaurant.cuisines, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "Rating: ${restaurant.rating} ⭐ | ETA: ${restaurant.etaMinutes} mins",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.onIntent(RestaurantDiscoveryIntent.PreviousPage) },
                                enabled = state.currentPage > 0
                            ) {
                                Text("Previous")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            val totalPages = (state.totalCount + state.pageSize - 1) / state.pageSize
                            Text("Page ${state.currentPage + 1} of $totalPages")
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { viewModel.onIntent(RestaurantDiscoveryIntent.NextPage) },
                                enabled = (state.currentPage + 1) * state.pageSize < state.totalCount
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
        }
    }
}
