package com.example.leofindit.deviceScanner.data

import com.example.leofindit.deviceScanner.data.database.AppDatabase
import com.example.leofindit.deviceScanner.data.database.BTLEDeviceDao
import com.example.leofindit.deviceScanner.data.database.BTLEDeviceEntity
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.deviceScanner.domain.DataRepository
import com.example.leofindit.errors.DataError
import com.example.leofindit.errors.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map

class DeviceRepository(
    private val database : AppDatabase,
    private val dao : BTLEDeviceDao,
    private val scanner : DeviceScanner
) : DataRepository{

    override suspend fun scannedDevices(): Flow<Result<List<BtleDevice>, DataError.DbError>> {
        val dataBaseResult : Flow<List<BTLEDeviceEntity>> = dao.getAllDevices()
        val scannerResult : Flow<List<BtleDevice>> = scanner.scanResults
        return combine(dataBaseResult, scannerResult) { dbEntity, scannerResult ->
            var combinedList = mutableListOf<BtleDevice>()
            scannerResult.forEach { scanned ->
                val dbMatch = dbEntity.find { it.deviceAddress == scanned.deviceAddress } ?: return@forEach
                val updatedTimeStamp  = dbMatch.timestamp.toMutableList().apply {
                    add(scanned.timeStamp)
                }
                val updatedDbDevice = dbMatch.copy(timestamp = updatedTimeStamp)
                dao.upsert(updatedDbDevice)
                combinedList.add(scanned.copy(isSuspicious = dbMatch.isSuspicious,
                    nickName = dbMatch.deviceNickname ))
            }
            Result.Success(combinedList) as Result<List<BtleDevice>,DataError.DbError>
        }.catch {
            emit(Result.Error(DataError.DbError.UNKNOWN))
        }



    }


    override suspend fun getDataBaseDevices(): Flow<List<BtleDevice>> {
        return dao.getAllDevices().map { entityList ->
            entityList.map { it.toBtleDevice() }
        }
    }

    override suspend fun getWhiteList(): Flow<List<BtleDevice>> {
        return dao.getSafeDevices().map{list ->
            list.map { it.toBtleDevice() }
        }
    }

    override suspend fun getBlackList(): Flow<List<BtleDevice>> {
        return dao.getSusDevices().map { list->
            list.map { it.toBtleDevice() }
        }
    }


    override suspend fun addDevice(device: BtleDevice) {
        dao.upsert(device.toEntity())
    }

    override suspend fun updateDevice(device: BtleDevice) {
        dao.upsert(device.toEntity())
    }

    override suspend fun deleteDevice(device: BtleDevice) {
        dao.delete(device.toEntity())
    }

    override suspend fun deleteAllDevices() {
        dao.deleteAll()
    }

    override suspend fun getDeviceByAddress(address: String): BtleDevice? {
        return dao.getDeviceByAddress(address)?.toBtleDevice()
    }

//    override fun trackDevice() {
//        TODO("Not yet implemented")
//    }

}