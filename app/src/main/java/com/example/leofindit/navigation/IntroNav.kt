package com.example.leofindit.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface IntroNav {
    @Serializable
    data object IntroRouteGraph : IntroNav

    @Serializable
    data object Introduction : IntroNav

    @Serializable
    data object LocationPermission : IntroNav

    @Serializable
    data object BluetoothPermission : IntroNav

    @Serializable
    data object NotificationAccess : IntroNav

    @Serializable
    data object PermissionsDone : IntroNav
}

