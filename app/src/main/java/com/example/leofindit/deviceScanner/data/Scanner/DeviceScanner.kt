package com.example.leofindit.deviceScanner.data.Scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.util.size
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.errors.DataError
import com.example.leofindit.errors.EmptyResult
import com.example.leofindit.errors.Result
import com.example.leofindit.errors.onError
import com.example.leofindit.errors.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class DeviceScanner(private val context: Context) {

    var tag: String? = "DeviceScanner"

    // Initialize Bluetooth interface(s)
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var isScanning = false
    private val _scanResults = MutableStateFlow<MutableList<BtleDevice>>(mutableListOf())
    val scanResults : StateFlow<List<BtleDevice>> = _scanResults.asStateFlow()
    private var scanCallback: ((List<BtleDevice>) -> Unit)? = null
    private var bluetoothGatt : BluetoothGatt? = null
    private val GENERIC_ACCESS_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
    private val DEVICE_NAME_UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    private val MANUFACTURER_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")


    init {
        Log.d(tag, "BluetoothAdapter: $bluetoothAdapter")
        Log.d(tag, "BluetoothLeScanner: $bluetoothLeScanner")
        isScanning = false
    }

    fun mutateDeviceNickName(address : String, newNickName : String) {
        _scanResults.update { currentMutableList ->
            val newList = currentMutableList.map { device ->
                if (device.deviceAddress == address) {
                    device.copy(nickName = newNickName)
                } else {
                    device
                }
            }.toMutableList()
            newList
        }
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
        val currentList = _scanResults.value
        val mutableList = currentList.toMutableList() // Create a mutable copy
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

        var companyId : Int = -1
        var manufacturerByteData : ByteArray = byteArrayOf()
        val manufactureSpecificData = scanRecord?.manufacturerSpecificData
        manufactureSpecificData?.let {
            if(it.size > 0) {
                for(i in 0 until it.size) {
                    companyId = it.keyAt(i)
                    manufacturerByteData = it.valueAt(i)
                    Log.d("Scanner", "device:$deviceName")
                    Log.d("Scanner", "company ID: $companyId")
                    Log.d("Scanner", "manufacturerByteData: $manufacturerByteData")
                }
            }
        }


        // Check if a device with this address already exists
        val existingDeviceIndex =
            mutableList.indexOfFirst { it.deviceAddress == deviceAddress }

        val btleDevice = BtleDevice(
            deviceType = deviceType,
            deviceManufacturer = if (companyId == -1) "" else String.format("0x%04X", companyId),
            deviceName = deviceName ?: "Unknown Device",
            deviceAddress = deviceAddress,
            signalStrength = rssi,
            timeStamp = System.currentTimeMillis(),
            nickName = device.alias,
            deviceUuid = uuids, // Store UUIDs as a comma-separated string
            manufacturerData = manufacturerByteData.toString()
        )

        // Update the scanResults list
        if (existingDeviceIndex == -1) {
            // Device is new, add it to the list
            mutableList.add(btleDevice)
        } else {
            // Device exists, update its data (e.g., RSSI)
            val existingDevice = mutableList[existingDeviceIndex]
            mutableList[existingDeviceIndex] = existingDevice.copy(signalStrength = btleDevice.signalStrength)
            // Update other properties as needed (e.g., if you want to update the device name from scan)
            // existingDevice.deviceName = btleDevice.deviceName
        }
        _scanResults.value= mutableList
        //scanCallback?.onScanResult(scanResults)
        scanCallback?.invoke((_scanResults.value.toList()))
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

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
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
    fun stopScanning() : EmptyResult<DataError.ScanningError> {
        if(
            (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
            ) {
            return Result.Error(DataError.ScanningError.MISSING_PERMISSIONS)
        }
        tag = "DeviceScanner.stopScanning()"
        Log.d(tag, "stopScanning called")
        if (!isScanning) return Result.Error(DataError.ScanningError.ALREADY_SCANNING)
        isScanning = false
        bluetoothLeScanner?.stopScan(leScanCallback)
        return Result.Success(Unit)
    }// End of stopScanning()

    fun connectToDevice(address: String) : Result<BluetoothDevice, DataError.ScanningError> {
        var device : BluetoothDevice
        bluetoothAdapter?.let { adapter ->
            try {
                device = adapter.getRemoteDevice(address)
            } catch (_: IllegalArgumentException) {
                return Result.Error(DataError.ScanningError.DEVICE_NOT_FOUND)
            }
        }?: run {
                return Result.Error(DataError.ScanningError.BLUETOOTH_DISABLED)
            }
        return Result.Success(device)
    }
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val deviceAddress = gatt?.device?.address ?: "Unknown Device"
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("Gatt", "Connected to Gatt server on device : $deviceAddress")
                if(ContextCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED
                )
                {
                    Log.e("Gatt", "Permission Error for device $deviceAddress")
                    gatt?.disconnect()
                    return
                }
                val discoverServicesResult = gatt?.discoverServices()
                Log.d("Gatt", "Attempting to start service discovery: $discoverServicesResult")
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("Gatt", "Disconnected from Gatt server")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e("Gatt", "Missing BLUETOOTH_CONNECT permission for onServicesDiscovered")
                gatt?.disconnect()
                gatt?.close()
                return
            }

            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                Log.d("Gatt", "Services Discovered Successfully. Attempting to read characteristics.")

                val genericAccessService = gatt.getService(GENERIC_ACCESS_UUID)

                if (genericAccessService == null) {
                    Log.w("Gatt", "Generic Access Service (0x1800) not found on this device.")
                    gatt.disconnect()
                    gatt.close()
                    return // Can't proceed without this service if you need characteristics from it
                }

                // --- Read Device Name Characteristic ---
                val deviceNameCharacteristic = genericAccessService.getCharacteristic(DEVICE_NAME_UUID)
                if (deviceNameCharacteristic != null) { // <--- CRUCIAL NULL CHECK
                    if ((deviceNameCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                        Log.d("Gatt", "Attempting to read Device Name characteristic.")
                        if (!gatt.readCharacteristic(deviceNameCharacteristic)) { // This is likely around your line 275
                            Log.e("Gatt", "Failed to *initiate* read for Device Name characteristic.")

                        }
                    } else {
                        Log.w("Gatt", "Device Name characteristic (0x2A00) is not readable.")
                    }
                } else {
                    Log.w("Gatt", "Device Name characteristic (0x2A00) not found in Generic Access service.")
                }

                // --- Read Manufacturer Name Characteristic ---
                val manufacturerCharacteristic = genericAccessService.getCharacteristic(MANUFACTURER_UUID)
                if (manufacturerCharacteristic != null) { // <--- CRUCIAL NULL CHECK
                    if ((manufacturerCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                        Log.d("Gatt", "Attempting to read Manufacturer Name characteristic.")
                        if (!gatt.readCharacteristic(manufacturerCharacteristic)) {
                            Log.e("Gatt", "Failed to *initiate* read for Manufacturer Name characteristic.")
                        }
                    } else {
                        Log.w("Gatt", "Manufacturer Name characteristic (0x2A29) is not readable.")
                    }
                } else {
                    Log.w("Gatt", "Manufacturer Name characteristic (0x2A29) not found in Generic Access service.")
                }
            } else {
                Log.w("Gatt", "Service discovery failed with status: $status or gatt is null")
                gatt?.disconnect()
                gatt?.close()
                bluetoothGatt = null
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    DEVICE_NAME_UUID -> {
                        val name = try {
                            value.toString(Charsets.UTF_8).trim()
                        } catch (e: Exception) {
                            Log.e("Gatt", "Error converting Manufacturer value to a string", e)
                        }
                        Log.d("Gatt", "Device Name From Gatt: $name")
                    }
                    MANUFACTURER_UUID -> {
                        val manu = try {
                            value.toString(Charsets.UTF_8).trim()
                        } catch(e : Exception) {
                            Log.e("Gatt", "Error in reading Manufacturer Name", e)
                        }
                        Log.d("Gatt", "Device Manufacturer: $manu")
                    }
                    else -> {
                        val genericChar = value.toHexString()
                        Log.d("Gatt", "Unhandled Characteristic value: $genericChar")
                    }
                }
            }
        }
    }
    fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

    fun interrogation(address: String) : EmptyResult<DataError.ScanningError> {
        Log.d("Gatt", "Interrogation function called for device: $address")
        connectToDevice(address = address)
            .onError { error ->
            return Result.Error(error)
        }
            .onSuccess { device ->
                if(
                    ContextCompat
                        .checkSelfPermission(
                             context,
                             Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ){
                    return Result.Error(DataError.ScanningError.MISSING_PERMISSIONS)
                }
                this.bluetoothGatt?.disconnect()
                this.bluetoothGatt?.close()
                this.bluetoothGatt = device.connectGatt(context,false,gattCallback)
            }
        return Result.Success(Unit)
    }
//
//    fun getScanState():Boolean{
//        return isScanning
}