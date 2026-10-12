package com.mrd.restaurant.discovery.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.ui.NavDisplay
import com.mrd.restaurant.discovery.demo.navigation.AppNavigator
import com.mrd.restaurant.discovery.demo.ui.theme.TakeHomeDemoTheme
import org.koin.android.ext.android.get
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.compose.navigation3.getEntryProvider
import org.koin.androidx.scope.activityRetainedScope
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.scope.Scope

class MainActivity : ComponentActivity(), AndroidScopeComponent {

    override val scope: Scope by activityRetainedScope()

    @OptIn(KoinExperimentalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigator: AppNavigator = get()
            TakeHomeDemoTheme {
                NavDisplay(
                    backStack = navigator.backStack,
                    onBack = {
                        navigator.navigateBack()
                    },
                    entryProvider = getEntryProvider()
                )
            }
        }
    }
}
