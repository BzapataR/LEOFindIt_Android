package com.example.leofindit.deviceScanner.data

import com.example.leofindit.deviceScanner.data.database.AppDatabase

class DeviceRepository(
    private val database : AppDatabase,
    private val scanner : DeviceScanner
) {

}