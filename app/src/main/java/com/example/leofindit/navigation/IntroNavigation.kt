package com.example.leofindit.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface IntroNavigation {

    data object IntroRouteGraph : IntroNavigation

    @Serializable
    data object Introduction : IntroNavigation

    @Serializable
    data object LocationPermission : IntroNavigation

    @Serializable
    data object BluetoothPermission : IntroNavigation

    @Serializable
    data object NotificationAccess : IntroNavigation

    @Serializable
    data object PermissionsDone : IntroNavigation
}

