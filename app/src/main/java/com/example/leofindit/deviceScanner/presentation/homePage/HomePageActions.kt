package com.example.leofindit.deviceScanner.presentation.homePage

import com.example.leofindit.deviceScanner.domain.BtleDevice

sealed interface HomePageActions {
    data object startScan : HomePageActions
    data object pauseScan : HomePageActions
    data class onDeviceClick(val device: BtleDevice) : HomePageActions
    data object onSettingsButtonClick : HomePageActions
    data object toMarkedDevices : HomePageActions
}