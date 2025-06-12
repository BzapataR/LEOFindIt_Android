package com.example.leofindit.navigation

import com.example.leofindit.preferences.UserPreferencesRepository
import kotlinx.serialization.Serializable

sealed interface MainNavigation {
    @Serializable
    data object MainNavGraph: MainNavigation

    @Serializable
    data object ManualScan : MainNavigation

    @Serializable
    data class TrackerDetails(val address : String) : MainNavigation

    @Serializable
    data class PrecisionFinding(val address: String) : MainNavigation

    @Serializable
    data object Settings : MainNavigation

    @Serializable
    data object AppInfo: MainNavigation

    @Serializable
    data object ObserveTracker : MainNavigation

    @Serializable
    data object MarkedDevice : MainNavigation

    @Serializable
    data object ThemeDialog : MainNavigation
}