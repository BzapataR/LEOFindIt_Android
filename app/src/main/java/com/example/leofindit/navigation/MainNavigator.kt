package com.example.leofindit.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Right
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.leofindit.deviceScanner.presentation.SelectedDeviceViewModel
import com.example.leofindit.deviceScanner.presentation.databaseDevices.DatabaseDeviceRoot
import com.example.leofindit.deviceScanner.presentation.databaseDevices.DatabaseDeviceViewModel
import com.example.leofindit.deviceScanner.presentation.homePage.HomePageRoot
import com.example.leofindit.deviceScanner.presentation.homePage.HomePageViewModel
import com.example.leofindit.deviceScanner.presentation.settings.AppInfo
import com.example.leofindit.deviceScanner.presentation.settings.Settings
import com.example.leofindit.deviceScanner.presentation.trackerDetails.TrackerDetailViewModel
import com.example.leofindit.deviceScanner.presentation.trackerDetails.TrackerDetailsRoot
import org.koin.compose.viewmodel.koinViewModel

/*********************************************************************************
 *                   Main nav host, add any pages here
 *********************************************************************************/
@Composable
fun MainNavigator(
    navigator : NavHostController,
) {
    NavHost(
        navController = navigator,
        startDestination = MainNavigation.MainNavGraph,
        enterTransition = { slideIntoContainer(Left, tween(500)) },
        exitTransition = { slideOutOfContainer(Left, tween(500)) },
        popEnterTransition = { slideIntoContainer(Right, tween(500)) },
        popExitTransition = { slideOutOfContainer(Right, tween(500)) }
    ) {
        navigation<MainNavigation.MainNavGraph>(
            startDestination = MainNavigation.ManualScan
        ) {

            composable<MainNavigation.ManualScan>
            {
                val homePageViewModel = koinViewModel<HomePageViewModel>()
                val selectedDeviceViewModel =
                    it.sharedKoinViewModel<SelectedDeviceViewModel>(navigator)
                LaunchedEffect(Unit) {
                    selectedDeviceViewModel.onSelectedDevice(null)
                }
                HomePageRoot(
                    viewModel = homePageViewModel,
                    onDeviceClick = { device ->
                        selectedDeviceViewModel.onSelectedDevice(device)
                        navigator.navigate(
                            MainNavigation.TrackerDetails(address = device.deviceAddress)
                        )
                    },
                    onSettingsClick = { navigator.navigate(MainNavigation.Settings) },
                    toMarkedDevices = { navigator.navigate(MainNavigation.MarkedDevice) }
                )
            }

            composable<MainNavigation.TrackerDetails> {
                val trackerDetailViewModel = koinViewModel<TrackerDetailViewModel>()
                val selectedDeviceViewModel =
                    it.sharedKoinViewModel<SelectedDeviceViewModel>(navigator)
                val selectedDevice
                        by selectedDeviceViewModel.selectedDevice.collectAsStateWithLifecycle()
                LaunchedEffect(selectedDevice) {
                    selectedDevice.let {
                        selectedDeviceViewModel.onSelectedDevice(it)
                    }
                }
                TrackerDetailsRoot(
                    viewModel = trackerDetailViewModel,
                    goBack = { navigator.popBackStack() }
                )
            }

            composable<MainNavigation.Settings>{
                Settings(
                    goBack = { navigator.popBackStack() },
                    toAppInfo = { navigator.navigate(MainNavigation.AppInfo) },
                    toSavedDevices = { navigator.navigate(MainNavigation.MarkedDevice) }
                )
            }
            composable<MainNavigation.AppInfo> {
                AppInfo(
                    goBack = { navigator.popBackStack() }
                )
            }
            composable<MainNavigation.MarkedDevice> {
                val databaseViewModel = koinViewModel<DatabaseDeviceViewModel>()
                val selectedDeviceViewModel =
                    it.sharedKoinViewModel<SelectedDeviceViewModel>(navigator)
                DatabaseDeviceRoot(
                    viewModel = databaseViewModel,
                    goBack = { navigator.popBackStack() },
                    onDeviceClicked = { device ->
                        selectedDeviceViewModel.onSelectedDevice(device)
                        navigator.navigate(
                            MainNavigation.TrackerDetails(address = device.deviceAddress)
                        )
                    }
                )
            }

        }
    }
}

@Composable // to share view models between composable
private inline fun <reified T: ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )
}