package com.example.leofindit.di
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.leofindit.deviceScanner.data.DeviceRepository
import com.example.leofindit.deviceScanner.data.DeviceScanner
import com.example.leofindit.deviceScanner.data.database.AppDatabase
import com.example.leofindit.deviceScanner.domain.DataRepository
import com.example.leofindit.deviceScanner.presentation.SelectedDeviceViewModel
import com.example.leofindit.deviceScanner.presentation.databaseDevices.DatabaseDeviceViewModel
import com.example.leofindit.deviceScanner.presentation.homePage.HomePageViewModel
import com.example.leofindit.deviceScanner.presentation.trackerDetails.TrackerDetailViewModel
import com.example.leofindit.preferences.UserPreferences
import com.example.leofindit.preferences.UserPreferencesRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val Context.appSettingsDataStore : DataStore<Preferences> by preferencesDataStore(
    name = "App Settings"
)

val modules = module {
    single{
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "btle_device_database",
        ).build()
    }
    single{ get<AppDatabase>().btleDeviceDao() }
    single{ DeviceScanner(androidContext()) }
    single<DataStore<Preferences>> {
        androidApplication().appSettingsDataStore // Use the application context to get the DataStore
    }
    single{ UserPreferencesRepository(get()) }
    singleOf(::DeviceRepository).bind<DataRepository>()
    viewModelOf(::HomePageViewModel)
    viewModelOf(::SelectedDeviceViewModel)
    viewModelOf(::TrackerDetailViewModel)
    viewModelOf(::DatabaseDeviceViewModel)
}