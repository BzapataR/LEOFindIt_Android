package com.example.leofindit.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.leofindit.deviceScanner.data.DeviceScanner
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.deviceScanner.data.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//view model to store scanned device list and scanning logic
class ScanningViewModel(application: Application) : AndroidViewModel(application) {
    private val _scannedDevices = MutableStateFlow<List<BtleDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BtleDevice>> = _scannedDevices

    private val _isScanning = MutableStateFlow(false) // Keep track of scanning state
    val isScanning: StateFlow<Boolean> = _isScanning

    private val deviceScanner = DeviceScanner(application.applicationContext)



    init {
        // Set callback to receive scan results
        deviceScanner.setScanCallback { devices ->
            _scannedDevices.value = devices
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScanning() {
        Log.i("scanner", "Scan Starting...")
        _isScanning.value = true
        viewModelScope.launch {
            deviceScanner.startScanning()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScanning(targetAddress: String? = null) {
        Log.i("scanner", "Scan Starting...")
        _isScanning.value = true

        viewModelScope.launch {
            deviceScanner.startScanning()
        }
    }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScanning() {
        Log.i("scanner", "Scan Stopped.")
        deviceScanner.stopScanning()
        _isScanning.value = false
        Log.i("scanner", "${scannedDevices.value}")

    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun isScanning(): Boolean {
        return deviceScanner.getScanState()
    }
    fun interrogateDevice(address: String) {
        deviceScanner.interrogateDevice(address)
    }

    // Update a device state safely using copy()
    @SuppressLint("SuspiciousIndentation")
    fun updateDeviceState(address: String, isSuspicious: Boolean?): BtleDevice {
        val device: BtleDevice = _scannedDevices.value.find { it.deviceAddress == address }
            ?: throw NoSuchElementException("No device found with address : $address")
        when (isSuspicious) {
            false -> device.markSafe()
            true -> device.markSuspicious()
            null -> device.markNeutral()
        }
        Log.i("Device Call out", "Device: ${device.deviceName}, is suspicious = ${device.getIsSuspicious()}")
        return device
    }
    // to see if device is marked sus or safe
    fun isDeviceMarked(device: BtleDevice) : Boolean {
        return(device.getIsSuspicious() != null)
    }


    // Set nickname
    fun setNickName(address: String, newNickName: String) {
        _scannedDevices.value = _scannedDevices.value.map { device ->
            if (device.deviceAddress == address) {
                Log.i("BtleViewModel", "User renamed ${device.deviceType}: ${device.getNickName()} to $newNickName.")
                device.copy(nickName = newNickName)
            } else device
        }
    }

    //finds device based on device address
    fun findDevice(address: String): BtleDevice {
        return   _scannedDevices.value.find { it.deviceAddress == address }
            ?: throw NoSuchElementException("No device found with address: $address")
    }


    suspend fun insertOrUpdateAndSync(device: BtleDevice, database : AppDatabase): BtleDevice {
        val address = device.deviceAddress!!
        val existing = database.btleDeviceDao().getDeviceByAddress(address) ?: return device

        // Append current timestamp to list
        val updatedTimestamps = existing.timestamp.toMutableList().apply {
            add(device.timeStamp)
        }

        // Save updated entity to DB
        val updatedEntity = existing.copy(
            timestamp = updatedTimestamps,
        )
        database.btleDeviceDao().insert(updatedEntity)

        // This is the real updated device we want to return
        val updatedDevice = device.copy(
            nickName = updatedEntity.deviceNickname,
            isSuspicious = updatedEntity.isSuspicious,
        )

        Log.i("DataBase Device record", "DB device: $updatedEntity")
        Log.i("Device update", "Device update from DB, new device: $updatedDevice")
        return updatedDevice
    }

}