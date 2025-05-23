package com.example.leofindit.deviceScanner.presentation.homePage

import com.example.leofindit.deviceScanner.domain.BtleDevice

data class HomePageState(
    val isScanning : Boolean = false,
    val deviceList : List<BtleDevice> = emptyList(),
    val missingPermissions : Boolean = false
)