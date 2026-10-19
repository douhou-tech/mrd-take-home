package com.mrd.restaurant.discovery.ui.navigation

import android.content.Intent

interface BackNavigator {
    fun navigateBack()
}

interface DeepLinkNavigator {
    fun handleDeepLink(intent: Intent)
}