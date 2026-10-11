package com.mrd.restaurant.discovery.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mrd.restaurant.discovery.demo.ui.theme.TakeHomeDemoTheme
import com.mrd.restaurant.discovery.feature.ui.RestaurantDiscoveryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TakeHomeDemoTheme {
                RestaurantDiscoveryScreen()
            }
        }
    }
}