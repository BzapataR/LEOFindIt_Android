package com.example.leofindit.deviceScanner.domain

import com.example.leofindit.errors.DataError
import com.example.leofindit.errors.EmptyResult
import com.example.leofindit.errors.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface DataRepository {
    val _observableDevices: MutableStateFlow<List<BtleDevice>>
    val observableDevices : StateFlow<List<BtleDevice>>
    fun startScanning() : Result<Flow<List<BtleDevice>>, DataError.ScanningError>
    fun stopScanning() : EmptyResult<DataError.ScanningError>
    suspend fun getDataBaseDevices() : Flow<List<BtleDevice>>
    fun getWhiteList() : Flow<List<BtleDevice>>
    fun getBlackList() : Flow<List<BtleDevice>>
    suspend fun addDevice(device: BtleDevice)
    suspend fun updateDevice(device: BtleDevice)
    suspend fun deleteDevice(device: BtleDevice)
    suspend fun deleteAllDevices()
    fun getDeviceByAddress(address : String): BtleDevice?
    fun getDeviceAsFlow(address : String) : Flow<BtleDevice?>
    suspend fun editNickName(address: String, newNickName : String): EmptyResult<DataError.DbError>
    suspend fun editDeviceSus(address : String, newSusValue : Boolean?) : EmptyResult<DataError>
    //fun trackDevice()


}