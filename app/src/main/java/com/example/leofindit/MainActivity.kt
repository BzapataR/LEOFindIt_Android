// MainActivity.kt

package com.example.leofindit

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Right
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.leofindit.Intro.presentation.introduction.BluetoothPermission
import com.example.leofindit.Intro.presentation.introduction.Introduction
import com.example.leofindit.Intro.presentation.introduction.LocationAccess
import com.example.leofindit.Intro.presentation.introduction.NotificationPermission
import com.example.leofindit.Intro.presentation.introduction.PermissionsDone
import com.example.leofindit.controller.BtHelper
import com.example.leofindit.controller.LocationHelper
import com.example.leofindit.deviceScanner.presentation.SelectedDeviceViewModel
import com.example.leofindit.deviceScanner.presentation.databaseDevices.DatabaseDeviceRoot
import com.example.leofindit.deviceScanner.presentation.databaseDevices.DatabaseDeviceViewModel
import com.example.leofindit.deviceScanner.presentation.homePage.HomePageRoot
import com.example.leofindit.deviceScanner.presentation.homePage.HomePageViewModel
import com.example.leofindit.deviceScanner.presentation.settings.AppInfo
import com.example.leofindit.deviceScanner.presentation.settings.Settings
import com.example.leofindit.deviceScanner.presentation.trackerDetails.TrackerDetailViewModel
import com.example.leofindit.deviceScanner.presentation.trackerDetails.TrackerDetailsRoot
import com.example.leofindit.navigation.IntroNav
import com.example.leofindit.navigation.MainNavigation
import com.example.leofindit.preferences.UserPreferencesRepository
import com.example.leofindit.ui.theme.Background
import com.example.leofindit.ui.theme.LeoFindItTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


const val BLUETOOTH_PERMISSIONS_REQUEST_CODE = 101
val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "App Settings")

class MainActivity : ComponentActivity() {
    val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "First Launch")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SupportAnnotationUsage", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
         var keepSplashScreenOn = true
        installSplashScreen().setKeepOnScreenCondition { keepSplashScreenOn }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeoFindItTheme {
                BtHelper.init(context = this)
                LocationHelper.locationInit(context = this)

                val mainNavController = rememberNavController()
                val introNavController = rememberNavController()
                val context = applicationContext
                val userSettings = remember { UserPreferencesRepository(context) }
                var isFirstLaunch by remember { mutableStateOf<Boolean?>(null) }
                LaunchedEffect(Unit) {
                    userSettings.isFirstLaunch.first().let {
                        isFirstLaunch = it
                        keepSplashScreenOn = false
                    }
                }

                Column(
                    modifier = Modifier
                        .background(Background)
                        .navigationBarsPadding()
                        .statusBarsPadding()
                ) {
                Spacer(Modifier.size(24.dp))
                    when (isFirstLaunch) {
                        true -> {
                            IntroNavigator(
                                introNavController,
                                onFinish = {
                                    lifecycleScope.launch {
                                        userSettings.setFirstLaunch(false)
                                        isFirstLaunch = false
                                    }
                                }
                            )
                        }
                        false -> {
                            MainNavigator(
                                navigator = mainNavController,
                            )
                        }
                        null -> { // with splash screen this shouldn't trigger but is here as an edge case
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

        }
    }
}
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
///*********************************************************************************
// *                   NavHost used for introduction only
// *                   used once on first launch. Only add
// *                   for one time pages.
// *********************************************************************************/

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun IntroNavigator(introNavCtrl: NavHostController, onFinish: () -> Unit) {
    NavHost(
        navController = introNavCtrl,
        startDestination = IntroNav.IntroRouteGraph
    ) {
        navigation<IntroNav.IntroRouteGraph>(
            startDestination = IntroNav.Introduction
        ) {
            composable<IntroNav.Introduction>(
                exitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popExitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popEnterTransition = { slideInHorizontally { fullWidth -> fullWidth } },
                enterTransition = { slideInHorizontally { fullWidth -> fullWidth } }
            ) {
                Introduction { introNavCtrl.navigate(IntroNav.LocationPermission) }
            }
            composable<IntroNav.LocationPermission>(
                exitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popExitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popEnterTransition = { slideInHorizontally { fullWidth -> fullWidth } },
                enterTransition = { slideInHorizontally { fullWidth -> fullWidth } }
            ) {
                LocationAccess { introNavCtrl.navigate(IntroNav.BluetoothPermission) }
            }
            composable<IntroNav.BluetoothPermission>(
                exitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popExitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popEnterTransition = { slideInHorizontally { fullWidth -> fullWidth } },
                enterTransition = { slideInHorizontally { fullWidth -> fullWidth } }
            ) {
                BluetoothPermission(
                    toNotification = {introNavCtrl.navigate(IntroNav.NotificationAccess)},
                    toFinish = {introNavCtrl.navigate(IntroNav.PermissionsDone)}
                )
            }
            composable<IntroNav.NotificationAccess>(
                exitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popExitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popEnterTransition = { slideInHorizontally { fullWidth -> fullWidth } },
                enterTransition = { slideInHorizontally { fullWidth -> fullWidth } }
            ) {
                NotificationPermission { introNavCtrl.navigate(IntroNav.PermissionsDone) }
            }
            composable<IntroNav.PermissionsDone>(
                exitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popExitTransition = { slideOutHorizontally { fullWidth -> fullWidth } },
                popEnterTransition = { slideInHorizontally { fullWidth -> fullWidth } },
                enterTransition = { slideInHorizontally { fullWidth -> fullWidth } }
            ) {
                PermissionsDone { onFinish() }
            }
        }
    }
}
