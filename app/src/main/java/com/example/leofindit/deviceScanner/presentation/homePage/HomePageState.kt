package com.example.leofindit.deviceScanner.presentation.homePage

import com.example.leofindit.deviceScanner.domain.BtleDevice

data class HomePageState(
    val isScanning : Boolean = false,
    val deviceList : List<BtleDevice> = emptyList<BtleDevice>(),
    val namedDeviceList : List<BtleDevice> = emptyList<BtleDevice>(),
    val unnamedDevices : List<BtleDevice> = emptyList<BtleDevice>(),
    val missingPermissions : Boolean = false,
    val error : String? = null,
    val isLoading : Boolean? = null
)