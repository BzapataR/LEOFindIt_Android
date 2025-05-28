package com.example.leofindit.di
import androidx.room.Room
import com.example.leofindit.deviceScanner.data.DeviceRepository
import com.example.leofindit.deviceScanner.data.DeviceScanner
import com.example.leofindit.deviceScanner.data.database.AppDatabase
import com.example.leofindit.deviceScanner.domain.DataRepository
import com.example.leofindit.deviceScanner.presentation.SelectedDeviceViewModel
import com.example.leofindit.deviceScanner.presentation.homePage.HomePageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

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
    singleOf(::DeviceRepository).bind<DataRepository>()
    viewModelOf(::HomePageViewModel)
    viewModelOf(::SelectedDeviceViewModel)
}