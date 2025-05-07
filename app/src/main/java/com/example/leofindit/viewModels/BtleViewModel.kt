package com.example.leofindit.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.leofindit.model.AppDatabase
import com.example.leofindit.model.BtleDevice
import com.example.leofindit.model.DeviceScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.find
import kotlin.collections.map

//view model to store scanned device list and scanning logic
class BtleViewModel(application: Application) : AndroidViewModel(application) {
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

    // Update a device state safely using copy()
    @SuppressLint("SuspiciousIndentation")
    fun updateDeviceState(address: String, isSuspicious: Boolean?) {
        val device: BtleDevice = _scannedDevices.value.find { it.deviceAddress == address }
            ?: throw NoSuchElementException("No device found with address : $address")
        when (isSuspicious) {
            false -> device.markSafe()
            true -> device.markSuspicious()
            null -> device.markNeutral()
        }
        Log.i("Device Call out", "Device: ${device.deviceName}, is suspicious = ${device.getIsSuspicious()}")
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

}