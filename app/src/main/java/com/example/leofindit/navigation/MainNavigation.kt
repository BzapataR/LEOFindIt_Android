package com.example.leofindit.navigation

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
    data class DeviceFromDb(val address : String) : MainNavigation

    @Serializable
    data object MarkedDevice : MainNavigation
}