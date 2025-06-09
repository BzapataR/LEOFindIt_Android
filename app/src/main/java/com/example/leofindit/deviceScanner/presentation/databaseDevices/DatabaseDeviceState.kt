package com.example.leofindit.deviceScanner.presentation.databaseDevices

import com.example.leofindit.deviceScanner.domain.BtleDevice

data class DatabaseDeviceState (
    val isWhiteListOpen : Boolean = true,
    val isBlackListOpen : Boolean = true,
    val whiteListDevice : List<BtleDevice> = emptyList<BtleDevice>(),
    val blackListDevice : List<BtleDevice> = emptyList< BtleDevice>(),
    val displayDeletionRequest: Boolean = false
)