package com.example.leofindit.deviceScanner.data

import android.util.Log
import androidx.sqlite.SQLiteException
import com.example.leofindit.deviceScanner.data.database.BTLEDeviceDao
import com.example.leofindit.deviceScanner.data.database.BTLEDeviceEntity
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.deviceScanner.domain.DataRepository
import com.example.leofindit.errors.DataError
import com.example.leofindit.errors.DataError.DbError
import com.example.leofindit.errors.DataError.RepositoryError
import com.example.leofindit.errors.EmptyResult
import com.example.leofindit.errors.Result
import com.example.leofindit.errors.onError
import com.example.leofindit.errors.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class DeviceRepository(
    private val dao : BTLEDeviceDao,
    private val scanner : DeviceScanner,
) : DataRepository{
    val tag = "Device Repository"

    override val _observableDevices = MutableStateFlow(emptyList<BtleDevice>())
    override val observableDevices = _observableDevices.asStateFlow()
    val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        repositoryScope.launch {
           val devicesFromDb = dao.getAllDevices().first()
               .map { it.toBtleDevice() }
            _observableDevices.value = devicesFromDb
            Log.i("observable device update" , "${_observableDevices.value}")
        }
    }

    override fun startScanning(): Result<Flow<List<BtleDevice>>, DataError.ScanningError> {
        val dataBaseResult: Flow<List<BTLEDeviceEntity>> = dao.getAllDevices()
        val scannerResult: Flow<List<BtleDevice>> = scanner.scanResults
        scanner.startScanning().onSuccess {
            val combinedFlow : Flow<List<BtleDevice>> = combine(
                dataBaseResult.distinctUntilChanged(),
                scannerResult.distinctUntilChanged()
            ) { dbEntity, scannerResult ->
                scannerResult.map { scanned->
                    val dbMatch = dbEntity.find{ it.deviceAddress == scanned.deviceAddress }

                    if (dbMatch != null) {
                        repositoryScope.launch {
                            val updatedTimeStamp = dbMatch.timestamp.toMutableList().apply {
                                if(!contains(scanned.timeStamp)) { add(scanned.timeStamp) }
                            }
                            val updatedEntity = dbMatch.copy(timestamp = updatedTimeStamp)
                            dao.upsert(updatedEntity)
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
            repositoryScope.launch {
                combinedFlow.collect { deviceList ->
                    _observableDevices.value = deviceList
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


    override suspend fun getDataBaseDevices(): Flow<List<BtleDevice>> {
        return dao.getAllDevices().map { entityList ->
            entityList.map { it.toBtleDevice() }
        }
    }

    override fun getWhiteList(): Flow<List<BtleDevice>> {
        return dao.getSafeDevices().map{list ->
            list.map { it.toBtleDevice() }
        }
    }

    override fun getBlackList(): Flow<List<BtleDevice>> {
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

    override fun getDeviceByAddress(address: String): BtleDevice? {
        return _observableDevices.value.find { it.deviceAddress == address }
    }

    override fun getDeviceAsFlow(address: String): Flow<BtleDevice?> {
        return _observableDevices.map { deviceList ->
            deviceList.find { it.deviceAddress == address }
        }
            .distinctUntilChanged()
    }

    override suspend fun editNickName(address: String, newNickName: String) : EmptyResult<DbError> {
        val databaseDevice = dao.getDeviceByAddress(address)
        if(databaseDevice != null) {
            try {
                dao.upsert(databaseDevice.copy(deviceNickname = newNickName))
            } catch (_: SQLiteException) {
                return Result.Error(DbError.DISK_FULL)
            }
        }
        _observableDevices.update { currentList->
            currentList.map { deviceInList ->
                if(deviceInList.deviceAddress == address) {
                    return@map deviceInList.copy(nickName = newNickName)
                }
                else {
                    return@map deviceInList
                }
            }
        }
        return Result.Success(Unit)
    }

    override suspend fun editDeviceSus(
        address: String,
        newSusValue: Boolean?
    ): EmptyResult<DataError> {
        var databaseDevice = dao.getDeviceByAddress(address)
        if (databaseDevice != null) {
            if (newSusValue == null) {
                dao.delete(databaseDevice)
                return Result.Success(Unit)
            } // we don't want to store neutral values
            else {
                try {
                    dao.update(databaseDevice.copy(isSuspicious = newSusValue))
                } catch (_: SQLiteException) {
                    return Result.Error(DbError.DISK_FULL)
                }
            }
        }
        if (getDeviceByAddress(address) != null) {
            if (newSusValue != null) {
                try {
                    dao.insert(
                        getDeviceByAddress(address)!!.toEntity().copy(isSuspicious = newSusValue)
                    )
                } catch (_: SQLiteException) {
                    return Result.Error(DbError.DISK_FULL)
                }
                _observableDevices.update { currentList ->
                    currentList.map { deviceInList ->
                        if (deviceInList.deviceAddress == address) {
                            return@map deviceInList.copy(isSuspicious = newSusValue)
                        } else {
                            return@map deviceInList
                        }
                    }
                }
            }
            Log.i("newDeviceValue", "${getDeviceByAddress(address)?.isSuspicious}")
                return Result.Success(Unit)
        }
        else if(getDeviceByAddress(address) == null) {
            return Result.Error(RepositoryError.DEVICE_NOT_FOUND)
        }
        return Result.Error(DbError.UNKNOWN)
    }


    fun timeStampFormat(timeStamp: Long): String {
        val timeDiffMillis = System.currentTimeMillis() - timeStamp
        if (timeStamp > System.currentTimeMillis())
            return "Now"
        // Calculate total seconds, minutes, and hours from the difference
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(timeDiffMillis)
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis)
        val totalHours = TimeUnit.MILLISECONDS.toHours(timeDiffMillis)

        // Calculate the components for HH:MM:SS format
        val hoursComponent = totalHours
        val minutesComponent = totalMinutes % 60 // Minutes in the current hour (0-59)
        val secondsComponent = totalSeconds % 60 // Seconds in the current minute (0-59)

        return String.format(Locale.US,"%02d:%02d:%02d", hoursComponent, minutesComponent, secondsComponent)
    }



//    override fun trackDevice() {
//        TODO("Not yet implemented")
//    }

}