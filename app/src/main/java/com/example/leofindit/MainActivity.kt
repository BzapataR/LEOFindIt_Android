// MainActivity.kt

package com.example.leofindit

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.leofindit.controller.BtHelper
import com.example.leofindit.controller.LocationHelper
import com.example.leofindit.navigation.IntroNavigator
import com.example.leofindit.navigation.MainNavigator
import com.example.leofindit.preferences.ThemePreference
import com.example.leofindit.preferences.UserPreferencesRepository
import com.example.leofindit.ui.theme.LeoFindItTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SupportAnnotationUsage", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val userSettings : UserPreferencesRepository by inject()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme =  userSettings.getCurrentTheme.collectAsStateWithLifecycle(ThemePreference.SYSTEM)
            LeoFindItTheme(themeSettings = theme.value) {
                BtHelper.init(context = this)
                LocationHelper.locationInit(context = this)

                val mainNavController = rememberNavController()
                val introNavController = rememberNavController()

                var isFirstLaunch = userSettings.isFirstLaunch.collectAsStateWithLifecycle(null)
                splashScreen.setKeepOnScreenCondition { isFirstLaunch.value == null }
                LaunchedEffect(Unit) {
                    userSettings.isFirstLaunch.first().let {
                        userSettings.setFirstLaunch(it)
                    }
                }

                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .statusBarsPadding()
                ) {
                Spacer(Modifier.size(24.dp))
                    when (isFirstLaunch.value) {
                        true -> {
                            IntroNavigator(
                                introNavController,
                                onFinish = {
                                    lifecycleScope.launch {
                                        userSettings.setFirstLaunch(false)
                                    }
                                }
                            )
                        }
                        false -> {
                            MainNavigator(
                                navigator = mainNavController,
                                userPreferencesRepository = userSettings,
                                currentTheme = theme.value
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