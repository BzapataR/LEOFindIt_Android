package com.example.leofindit.deviceScanner.presentation.databaseDevices

import com.example.leofindit.deviceScanner.domain.BtleDevice

sealed interface DatabaseDevicesActions {
    data object goBack : DatabaseDevicesActions
    data class onDeviceClicked(val device : BtleDevice) : DatabaseDevicesActions
    data object OpenWhiteList : DatabaseDevicesActions
    data object OpenBlackList : DatabaseDevicesActions
    data object ToggleDeletionDialog : DatabaseDevicesActions
    data object DeleteSavedDevices : DatabaseDevicesActions
}