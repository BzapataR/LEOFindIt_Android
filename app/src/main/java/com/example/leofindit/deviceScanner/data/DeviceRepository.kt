package com.example.leofindit.deviceScanner.data

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.room.coroutines.createFlow
import androidx.sqlite.SQLiteException
import com.example.leofindit.deviceScanner.data.database.BTLEDeviceDao
import com.example.leofindit.deviceScanner.data.database.BTLEDeviceEntity
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.deviceScanner.domain.DataRepository
import com.example.leofindit.errors.DataError
import com.example.leofindit.errors.DataError.DbError
import com.example.leofindit.errors.EmptyResult
import com.example.leofindit.errors.Result
import com.example.leofindit.errors.onError
import com.example.leofindit.errors.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class DeviceRepository(
    private val dao : BTLEDeviceDao,
    private val scanner : DeviceScanner
) : DataRepository{


    override fun startScanning(): Result<Flow<List<BtleDevice>>, DataError.ScanningError> {
        val dataBaseResult: Flow<List<BTLEDeviceEntity>> = dao.getAllDevices()
        val scannerResult: Flow<List<BtleDevice>> = scanner.scanResults
        scanner.startScanning().onSuccess {
            val combinedFlow : Flow<List<BtleDevice>> = combine(dataBaseResult, scannerResult) { dbEntity, scannerResult ->
                scannerResult.map { scanned->
                    val dbMatch = dbEntity.find{ it.deviceAddress == scanned.deviceAddress }

                    if (dbMatch != null) {
                        coroutineScope {
                            launch {
                                val updatedTimeStamp = dbMatch.timestamp.toMutableList().apply {
                                    add(scanned.timeStamp)
                                }
                                val updatedEntity = dbMatch.copy(timestamp = updatedTimeStamp)
                                dao.upsert(updatedEntity)
                            }
                        }
                        scanned.copy(
                            isSuspicious = dbMatch.isSuspicious,
                            nickName = dbMatch.deviceNickname,
                        )
                    }
                    else
                        scanned
                }
            }
            return Result.Success(combinedFlow)
        }
            .onError {error ->
                return Result.Error(error)
            }


        return Result.Error(DataError.ScanningError.UNKNOWN_ERROR)
    }

    override fun stopScanning(): EmptyResult<DataError.ScanningError> {
        return scanner.stopScanning()
    }

    suspend fun combinedList(scannedValue : BtleDevice?, dbValue : BTLEDeviceEntity?) : BtleDevice? {
        if (scannedValue == null && dbValue == null){
            return null
        }
        if (scannedValue != null && dbValue != null) {
            coroutineScope {
                launch {
                    val updatedTimeStamp = dbValue.timestamp.toMutableList().apply {
                        add(scannedValue.timeStamp)
                    }
                    val updatedEntity = dbValue.copy(timestamp = updatedTimeStamp)
                    dao.upsert(updatedEntity)
                }
            }
            scannedValue.copy(
                isSuspicious = dbValue.isSuspicious,
                nickName = dbValue.deviceNickname,
            )
        }
        return scannedValue?: dbValue?.toBtleDevice()



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

    override fun getDeviceByAddress(address: String): Result<Flow<BtleDevice>, DataError.RepositoryError> {
        var notFound: Boolean = false
        val scanListDevice = scanner.findDeviceByAddress(address)
        val dataBaseDevice = dao.getDeviceByAddress(address)
        val foundDevice = combine(scanListDevice, dataBaseDevice ) { scanResult, dbResult ->
            combinedList(scanResult, dbResult)
        }.onEmpty { notFound = true }.filterNotNull()
        if(notFound) {
            return Result.Error(DataError.RepositoryError.DEVICE_NOT_FOUND)
        }
        else return Result.Success(foundDevice)
    }

    override suspend fun editNickName(address: String, newNickName: String) : EmptyResult<DbError> {
        var databaseDevice = dao.getDeviceByAddress(address).firstOrNull()
        if(databaseDevice!= null) {
            try {
                dao.upsert(databaseDevice.copy(deviceNickname = newNickName))
            } catch (_: SQLiteException) {
                return Result.Error(DbError.DISK_FULL)
            }
        }
        scanner.mutateDeviceNickName(address = address, newNickName = newNickName)
        return Result.Success(Unit)
    }

    override suspend fun editDeviceSus(
        address: String,
        newSusValue: Boolean?
    ): EmptyResult<DbError> {
        var databaseDevice = dao.getDeviceByAddress(address).firstOrNull()
        if(databaseDevice!= null) {
            try {
                if (newSusValue == null) { dao.delete(databaseDevice)} // we don't want to store neutral values
                dao.upsert(databaseDevice.copy(isSuspicious = newSusValue))
            } catch (_: SQLiteException) {
                return Result.Error(DbError.DISK_FULL)
            }
        }
        return Result.Success(Unit)
    }
    fun timeStampFormat(timeStamp: Long): String {
        val timeDiffMillis = System.currentTimeMillis() - timeStamp
        val hours = TimeUnit.MILLISECONDS.toHours(timeDiffMillis)
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis) - TimeUnit.HOURS.toMinutes(hours)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(timeDiffMillis) - TimeUnit.MINUTES.toSeconds(minutes)

        return String.format(Locale.US,"%02d:%02d:%02d", hours, minutes, seconds)
    }



//    override fun trackDevice() {
//        TODO("Not yet implemented")
//    }

}