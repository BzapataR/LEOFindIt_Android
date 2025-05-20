package com.example.leofindit.deviceScanner.domain

interface DataRepository {
    suspend fun getDevices()
    suspend fun addDevice(device: BtleDevice)
    suspend fun updateDevice(device: BtleDevice)
    suspend fun deleteDevice(device: BtleDevice)
    suspend fun deleteAllDevices()
    suspend fun getDeviceByAddress(address : String): BtleDevice?
    fun trackDevice()


}