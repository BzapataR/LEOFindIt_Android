package com.example.leofindit.deviceScanner.domain

import com.example.leofindit.errors.DataError
import com.example.leofindit.errors.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DataRepository {
    suspend fun scannedDevices() : Flow<Result<List<BtleDevice>, DataError.DbError>>
    suspend fun getDataBaseDevices() : Flow<List<BtleDevice>>
    suspend fun getWhiteList() : Flow<List<BtleDevice>>
    suspend fun getBlackList() : Flow<List<BtleDevice>>
    suspend fun addDevice(device: BtleDevice)
    suspend fun updateDevice(device: BtleDevice)
    suspend fun deleteDevice(device: BtleDevice)
    suspend fun deleteAllDevices()
    suspend fun getDeviceByAddress(address : String): BtleDevice?
    //fun trackDevice()


}