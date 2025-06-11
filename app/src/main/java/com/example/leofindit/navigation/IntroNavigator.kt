package com.example.leofindit.navigation

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Right
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.leofindit.Intro.presentation.bluetooth.BluetoothPermission
import com.example.leofindit.Intro.presentation.introText.Introduction
import com.example.leofindit.Intro.presentation.location.LocationAccess
import com.example.leofindit.Intro.presentation.notification.NotificationPermission
import com.example.leofindit.Intro.presentation.finishText.PermissionsDone


/**********************************************************************************
 *                   NavHost used for introduction only
 *                   used once on first launch. Only add
 *                   for one time pages.
 *********************************************************************************/

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun IntroNavigator(introNavCtrl: NavHostController, onFinish: () -> Unit) {
    NavHost(
        navController = introNavCtrl,
        startDestination = IntroNav.IntroRouteGraph
    ) {
        navigation<IntroNav.IntroRouteGraph>(
            startDestination = IntroNav.Introduction,
            enterTransition = { slideIntoContainer(Left, tween(500)) },
            exitTransition = { slideOutOfContainer(Left, tween(500)) },
            popEnterTransition = { slideIntoContainer(Right, tween(500)) },
            popExitTransition = { slideOutOfContainer(Right, tween(500)) }
        ) {
            composable<IntroNav.Introduction>{
                Introduction { introNavCtrl.navigate(IntroNav.LocationPermission) }
            }
            composable<IntroNav.LocationPermission> {
                LocationAccess { introNavCtrl.navigate(IntroNav.BluetoothPermission) }
            }
            composable<IntroNav.BluetoothPermission> {
                BluetoothPermission(
                    toNotification = {introNavCtrl.navigate(IntroNav.NotificationAccess)},
                    toFinish = {introNavCtrl.navigate(IntroNav.PermissionsDone)}
                )
            }
            composable<IntroNav.NotificationAccess> {
                NotificationPermission { introNavCtrl.navigate(IntroNav.PermissionsDone) }
            }
            composable<IntroNav.PermissionsDone> {
                PermissionsDone { onFinish() }
            }
        }
    }
}
