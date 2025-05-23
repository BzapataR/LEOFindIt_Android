package com.example.leofindit.deviceScanner.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.errors.DataError
import com.example.leofindit.errors.EmptyResult
import com.example.leofindit.errors.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class DeviceScanner(private val context: Context) {

    var tag: String? = "DeviceScanner"

    // Initialize Bluetooth interface(s)
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var isScanning = false
    val list = mutableListOf<BtleDevice>()
    private val _scanResults = MutableStateFlow<MutableList<BtleDevice>>(mutableListOf())
    val scanResults : StateFlow<List<BtleDevice>> = _scanResults.asStateFlow()
    private var scanCallback: ((List<BtleDevice>) -> Unit)? = null


    init {
        Log.d(tag, "BluetoothAdapter: $bluetoothAdapter")
        Log.d(tag, "BluetoothLeScanner: $bluetoothLeScanner")
        isScanning = false
    }

    fun setScanCallback(callback: (List<BtleDevice>) -> Unit) {
        scanCallback = callback
    }


    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            processResults(result = result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { result ->
                processResults(result = result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            return
        }
    }

    private fun processResults(result: ScanResult) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Permission Error", "Unknown Device (No BT Connect Permission)")
            return
        }
        val device = result.device
        val rssi = result.rssi
        val scanRecord = result.scanRecord


        val deviceName: String? = device.name ?: scanRecord?.deviceName
        val deviceAddress = device.address ?: "Unknown" // Get the MAC address


        // Extract UUIDs from ScanRecord
        val uuids: MutableList<String> = mutableListOf()
        scanRecord?.serviceUuids?.forEach { uuid ->
            uuids.add(uuid.toString())
        }
        //val uuidString = uuids.joinToString(", ")

        val deviceType = "Generic BLE Device" // Replace with logic to determine device type
        val manufacturerData = scanRecord?.manufacturerSpecificData
        val manufacturer = if (manufacturerData != null && manufacturerData.isNotEmpty()) {
            // Assuming you want the first manufacturer ID's data
            val manufacturerId = manufacturerData.keyAt(0)
            val manufacturerSpecificData = manufacturerData[manufacturerId]
            manufacturerSpecificData?.joinToString("") { "%02X".format(it) }
                ?: "Unknown" // Convert bytes to hex string
        } else {
            "Unknown"
        }

        // Check if a device with this address already exists
        val existingDeviceIndex =
            _scanResults.value.indexOfFirst { it.deviceAddress == deviceAddress }

        val btleDevice = BtleDevice(
            deviceType = deviceType,
            deviceManufacturer = manufacturer,
            deviceName = deviceName ?: "Unknown Device",
            deviceAddress = deviceAddress,
            signalStrength = rssi,
            timeStamp = System.currentTimeMillis(),
            nickName = device.alias,
            deviceUuid = uuids // Store UUIDs as a comma-separated string
        )

        // Update the scanResults list
        if (existingDeviceIndex == -1) {
            // Device is new, add it to the list
            _scanResults.value.add(btleDevice)
        } else {
            // Device exists, update its data (e.g., RSSI)
            val existingDevice = _scanResults.value[existingDeviceIndex]
            existingDevice.signalStrength = btleDevice.signalStrength
            // Update other properties as needed (e.g., if you want to update the device name from scan)
            // existingDevice.deviceName = btleDevice.deviceName
        }
        //scanCallback?.onScanResult(scanResults)
        scanCallback?.invoke((scanResults.value.toList()))
    }


//    companion object BleUuids {
//        val GENERIC_ACCESS_SERVICE: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
//        val DEVICE_NAME_CHARACTERISTIC: UUID = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb")
//    }

/**
Functions below will be in another file likely ViewModel to process intents and states
 */
//    /**
//     * This function contains the core logic for calling system method to start BT Scan.
//     */
    fun startScanning() : EmptyResult<DataError.ScanningError> {
        Log.d("DeviceScanner", "startScanning called")

        if(
            (ContextCompat.checkSelfPermission(context, Manifest.permission_group.NEARBY_DEVICES) != PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED)
            ) {
            return Result.Error(DataError.ScanningError.MISSING_PERMISSIONS)
        }
        if (isScanning) return Result.Error(DataError.ScanningError.ALREADY_SCANNING)

        if (
            (bluetoothAdapter == null)
            ||
            (bluetoothLeScanner == null)
            ) {
            return Result.Error(DataError.ScanningError.UNKNOWN_ERROR)
        }
            if (!bluetoothAdapter.isEnabled ) return Result.Error(DataError.ScanningError.BLUETOOTH_DISABLED)

        try {
            isScanning = true
            bluetoothLeScanner.startScan(leScanCallback)
            _scanResults.value.clear() // Clear existing results
            return Result.Success(Unit)
        }
        catch(_: Exception) {
            isScanning = false
            return Result.Error(DataError.ScanningError.SCANNER_FAILED)
        }

    } // End of startScanning() function
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScanning() {
        tag = "DeviceScanner.stopScanning()"
        Log.d(tag, "stopScanning called")
        if (!isScanning) return
        isScanning = false
        bluetoothLeScanner?.stopScan(leScanCallback)
    }// End of stopScanning()
//
//    fun getScanState():Boolean{
//        return isScanning
}
// End of DeviceScanner Class