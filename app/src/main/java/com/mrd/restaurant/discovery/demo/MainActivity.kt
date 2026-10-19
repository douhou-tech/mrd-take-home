package com.mrd.restaurant.discovery.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mrd.restaurant.discovery.demo.navigation.AppNavigator
import com.mrd.restaurant.discovery.ui.theme.TakeHomeDemoTheme
import com.mrd.restaurant.discovery.feature.di.LocalSharedTransitionScope
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
            val navigator: AppNavigator = get<AppNavigator>().apply {
                handleDeepLink(intent)
            }

            TakeHomeDemoTheme {
                SharedTransitionLayout {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this
                    ) {
                        NavDisplay(
                            backStack = navigator.backStack,
                            onBack = {
                                navigator.navigateBack()
                            },
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            entryProvider = getEntryProvider(),
                            transitionSpec = {
                                fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
                            },
                            popTransitionSpec = {
                                fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
                            },
                            predictivePopTransitionSpec = {
                                fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
                            }
                        )
                    }
                }
            }
        }
    }
}
