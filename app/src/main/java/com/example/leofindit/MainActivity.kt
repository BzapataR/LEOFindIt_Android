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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.leofindit.controller.BtHelper
import com.example.leofindit.controller.LocationHelper
import com.example.leofindit.deviceScanner.data.DeviceScanner
import com.example.leofindit.deviceScanner.data.database.AppDatabase
import com.example.leofindit.deviceScanner.data.database.DatabaseProvider
import com.example.leofindit.deviceScanner.presentation.homePage.ManualScanning
import com.example.leofindit.deviceScanner.presentation.introduction.BluetoothPermission
import com.example.leofindit.deviceScanner.presentation.introduction.Introduction
import com.example.leofindit.deviceScanner.presentation.introduction.LocationAccess
import com.example.leofindit.deviceScanner.presentation.introduction.NotificationPermission
import com.example.leofindit.deviceScanner.presentation.introduction.PermissionsDone
import com.example.leofindit.deviceScanner.presentation.settings.AppInfo
import com.example.leofindit.deviceScanner.presentation.settings.DeviceByDb
import com.example.leofindit.deviceScanner.presentation.settings.MarkedDevices
import com.example.leofindit.deviceScanner.presentation.settings.Settings
import com.example.leofindit.deviceScanner.presentation.trackerDetails.ObserveTracker
import com.example.leofindit.navigation.MainNavigation.PrecisionFinding
import com.example.leofindit.navigation.MainNavigation.TrackerDetails
import com.example.leofindit.preferences.UserPreferencesRepository
import com.example.leofindit.ui.theme.Background
import com.example.leofindit.ui.theme.LeoFindItTheme
import com.example.leofindit.viewModels.BtleDbViewModel
import com.example.leofindit.viewModels.ScanningViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


const val BLUETOOTH_PERMISSIONS_REQUEST_CODE = 101
val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "App Settings")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

class MainActivity : ComponentActivity() {
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    /*********************************************************************************
     *                   Device Bt scanning vars
     *********************************************************************************/
    internal lateinit var deviceScanner: DeviceScanner
    private var tag = "MainActivity"
    private val scanningViewModel: ScanningViewModel by viewModels()
    private lateinit var database: AppDatabase
    val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "First Launch")

    @SuppressLint("SupportAnnotationUsage", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
         var keepSplashScreenOn = true
        installSplashScreen().setKeepOnScreenCondition { keepSplashScreenOn }
        database = DatabaseProvider.getDatabase(context = this)
        tag = "MainActivity.onCreate()"
        super.onCreate(savedInstanceState)
        deviceScanner = DeviceScanner(this)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
        setContent {
            LeoFindItTheme {
                val btleDbViewModel : BtleDbViewModel = viewModel(factory = BtleDbViewModel.provideFactory(database))
                BtHelper.init(context = this)
                LocationHelper.locationInit(context = this)
                val mainNavController = rememberNavController()
                val introNavController = rememberNavController()
//                val showBottomBar = listOf("Manual Scan", "Settings", "App info")
//                val currentRoute by mainNavController.currentBackStackEntryFlow
//                    .map { it.destination.route }
//                    .collectAsState(initial = null)

                val context = applicationContext
                val userSettings = remember { UserPreferencesRepository(context) }
                var isFirstLaunch by remember { mutableStateOf<Boolean?>(null) }
                LaunchedEffect(Unit) {
                    userSettings.isFirstLaunch.first().let {
                        isFirstLaunch = it
                        keepSplashScreenOn = false
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    containerColor = Background,
                    topBar = {},
                    bottomBar = {},
                    floatingActionButton = {
                    },
                ) { innerPadding ->
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
                                mainNavigator = mainNavController,
                                viewModel = scanningViewModel,
                                dbViewModel = btleDbViewModel
                            )
                        }
                        null -> { // with splash screen this shouldn't trigger but is here as an edge case
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onDestroy() {
        super.onDestroy()
        deviceScanner.stopScanning()
    }
}
/*********************************************************************************
 *                   Main nav host, add any pages here
 *********************************************************************************/
@Composable
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@SuppressLint("SupportAnnotationUsage")
fun MainNavigator(
    mainNavigator : NavHostController,
    viewModel : ScanningViewModel,
    dbViewModel : BtleDbViewModel
) {
//    NavHost(
//        navController = mainNavigator,
//        startDestination = MainNavigation.MainNavGraph
//    ) {
//        navigation< MainNavigation.MainNavGraph>(
//            startDestination = MainNavigation.ManualScan
//        ) {
//            composable<MainNavigation.ManualScan>(
//                exitTransition = { slideOutHorizontally() },
//                popEnterTransition = { slideInHorizontally() }
//            ) {
//                ManualScanning(viewModel = viewModel)
//            }
//            composable<MainNavigation.TrackerDetails>(
//                enterTransition = {
//                    slideInHorizontally { initialOffset ->
//                        initialOffset
//                    }
//                },
//                exitTransition = {slideOutHorizontally{initialOffset ->
//                    initialOffset
//                }},
//                { backStackEntry : NavBackStackEntry ->
//                    val trackerDetailsRoute = backStackEntry.toRoute()< MainNavigation.TrackerDetails>()
//                    val address = trackerDetailsRoute.address
//                }
//            )
//        }
//    }
    NavHost(
        navController = mainNavigator,
        startDestination = "Manual Scan"
    ) {
        composable("Manual Scan")  {
            ManualScanning(
                navController = mainNavigator, viewModel = viewModel,
                selectedDeviceViewModel = TODO()
            )
        }
        composable ("Tracker Details/{address}", arguments = listOf(navArgument("address") {type =
            NavType.StringType}))
        { backStackEntry ->
            val  address = backStackEntry.arguments?.getString("address") ?:return@composable
            TrackerDetails(
                navController = mainNavigator,
                viewModel = viewModel,
                address = address,
                dbViewModel = dbViewModel
            )
        }
        composable("Precision Finding/{address}", arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: return@composable
            PrecisionFinding(navController = mainNavigator, viewModel = viewModel, address = address)
        }
        composable("Settings") {
            Settings(navController = mainNavigator)
        }
        composable ("App info") {
            AppInfo(navController = mainNavigator)
        }
        composable("Observe Tracker") {
            ObserveTracker(navController = mainNavigator)
        }
        composable ("Marked Devices"){
            MarkedDevices(navController = mainNavigator, dbViewModel=dbViewModel)
        }
        composable("Device From Db/{address}", arguments = listOf(navArgument("address"){type =
            NavType.StringType})
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: return@composable
            DeviceByDb(navController = mainNavigator, dbViewModel=dbViewModel, address = address)
        }
    }
}


/*********************************************************************************
 *                   NavHost used for introduction only
 *                   used once on first launch. Only add
 *                   for one time pages.
 *********************************************************************************/
@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun IntroNavigator(introNavController: NavHostController, onFinish: () -> Unit) {
    NavHost(
        navController = introNavController,
        startDestination = "Introduction"
    ) {
        composable("Introduction") {
            Introduction(navController = introNavController)
            //FilterSideSheet()
        }
        composable("Location Permission") {
            LocationAccess(navController = introNavController)
        }
        composable("Bluetooth Permission")  {
            BluetoothPermission(navController = introNavController)
        }
        //Notification permission is not needed for API > 33
        composable("Notification Access") {
            NotificationPermission(navController = introNavController)
        }
        composable("Permission Done") {
            PermissionsDone(onFinish = onFinish)
        }
    }
}
