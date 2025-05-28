package com.example.leofindit.deviceScanner.domain

fun BtleDevice.isUnNamed() : Boolean {
    return this.deviceName.isBlank() ||
            this.deviceName.equals("Unknown Device", ignoreCase = true)
}