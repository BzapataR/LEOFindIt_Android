package com.example.leofindit.deviceScanner.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.example.leofindit.deviceScanner.domain.BtleDevice


class DeviceScanner(private val context: Context) {

    var tag: String? = "DeviceScanner"

    // Initialize Bluetooth interface(s)
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var isScanning = false

    /**
     * @Note Disabled until logic completed for only storing Blackist/Whitelist
    // Initialize Database for persistent device storage
    private val database = AppDatabase.getDatabase(context)
    private val btleDeviceDao = database.btleDeviceDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)  // Use IO dispatcher for DB
*/

    init {
        Log.d(tag, "BluetoothAdapter: $bluetoothAdapter")
        Log.d(tag, "BluetoothLeScanner: $bluetoothLeScanner")
        isScanning = false
    }


    private val scanResults = mutableListOf<BtleDevice>()

    // Callback interface to notify about scan results
    interface ScanCallback {
        fun onScanResult(devices: List<BtleDevice>)
    }
    private var scanCallback: ((List<BtleDevice>) -> Unit)? = null
    fun setScanCallback(callback: (List<BtleDevice>) -> Unit) {
        scanCallback = callback
    }
//    private var scanCallback: ScanCallback? = null
//
//    fun setScanCallback(callback: ScanCallback) {
//        scanCallback = callback
//    }

    private val leScanCallback = object : android.bluetooth.le.ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED )
                {
                    Log.e("Permission Error", "Unknown Device (No BT Connect Permission)")
                    return
                }
            val device = result.device
            val rssi = result.rssi
            val scanRecord = result.scanRecord


            val deviceName : String? = device.name ?: scanRecord?.deviceName
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
                scanResults.indexOfFirst { it.deviceAddress == deviceAddress }

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
                scanResults.add(btleDevice)
            } else {
                // Device exists, update its data (e.g., RSSI)
                val existingDevice = scanResults[existingDeviceIndex]
                existingDevice.signalStrength = btleDevice.signalStrength
                // Update other properties as needed (e.g., if you want to update the device name from scan)
                // existingDevice.deviceName = btleDevice.deviceName
            }
            //scanCallback?.onScanResult(scanResults)
            scanCallback?.invoke((scanResults.toList()))
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, "Scan failed with error: $errorCode")
        }
    }// End of leScanCallback

    fun interrogateDevice(deviceAddress : String) {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: run {
            Log.e(tag, "Error device with address: $deviceAddress not found")
            return
        }
        Log.d(tag, "Interrogating device: ${device.address}")
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(tag, "Cannot connect connect: BLUETOOTH_CONNECT Permission denied")
            return
        }
        else {
            device.connectGatt(context, false, object: BluetoothGattCallback() {
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt?,
                    status: Int,
                    newState: Int
                ) {
                    if (newState == BluetoothProfile.STATE_CONNECTED && gatt != null) {
                        Log.d(tag, "Device Connected: ${gatt.device.address}! Discovering Services...")
                        if (!gatt.discoverServices()) {
                            Log.e(tag, "Failed to start service discovery for ${gatt.device.address}")
                            gatt.close() // Close if discovery can't be initiated
                        }
                    }
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d(tag, "Disconnected from ${device.address}")
                        // Ensure you update UI or internal state if connection is lost during interrogation
                        // You might want to call gatt?.close() here too to release resources if not already done
                    }
                    else if (status != BluetoothGatt.GATT_SUCCESS) {
                        Log.e(tag, "Connection state change error: $status for device: ${device.address}")
                        gatt?.close() // Close on definitive connection error
                    }
                }

                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                        Log.d(tag, "Services discovered for ${device.address}")

                        // getting uuids from gatt
                        val allUuids = mutableListOf<String>()
                        gatt.services.forEach { service ->
                            allUuids.add(service.uuid.toString())
                            Log.d(tag, "service for: ${service.uuid} = ${service.characteristics}")
                        }
                        Log.d(tag, "Device Name: $allUuids")

                        val deviceName =
                            gatt.device.name ?: "Unknown Device"
                        Log.d(tag, "Device Name: $deviceName")

                        val deviceNickName = gatt.device.alias
                        Log.d(tag, "Device Nick Name: $deviceNickName")

                        //Updating device within the scanResults
                        val existingDevice = scanResults.find { it.deviceAddress == device.address }
                        if (existingDevice != null) {
                            existingDevice.deviceUuid = allUuids
                            existingDevice.deviceName = deviceName
                            existingDevice.setNickName(deviceNickName.toString())
                            scanCallback?.invoke(scanResults.toList())
                        }
//                        else {
//                            // if device was not found before, should not trigger in application but added just in case
//                            val newDevice = BtleDevice(
//                                deviceType = "Generic BLE Device",
//                                deviceManufacturer = existingDevice?.deviceManufacturer.toString(),
//                                deviceName = deviceName,
//                                deviceAddress = device.address,
//                                signalStrength = 0,
//                                timeStamp = System.currentTimeMillis(),
//                                deviceUuid = allUuids.joinToString(", ")
//                            )
//                            scanResults.add(newDevice)
//                            scanCallback?.invoke(scanResults.toList())
//                        }
//                        existingDevice?.deviceUuid = allUuids.joinToString(", ")
//                        // Notify that the UUIDs are available
//                        scanCallback?.invoke(scanResults.toList())
                        gatt.close()
                    }
                    else{
                        Log.e(
                            tag,
                            "onServiceDiscovered failed with status: $status for device ${device.address}"
                            )
                        gatt?.close()
                    }
                }
            }
            )
        }
    }
//    companion object BleUuids {
//        val GENERIC_ACCESS_SERVICE: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
//        val DEVICE_NAME_CHARACTERISTIC: UUID = UUID.fromString("00002A00-0000-1000-8000-00805f9b34fb")
//    }

    /**
     * This function contains the core logic for calling system method to start BT Scan.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScanning() {
        Log.d("DeviceScanner", "startScanning called")
        if (isScanning) return

        if (bluetoothAdapter?.isEnabled == false) {
            Log.e("DeviceScanner", "Bluetooth is not enabled")
            return
        }

        isScanning = true
        scanResults.clear() // Clear existing results

        bluetoothLeScanner?.startScan(leScanCallback)

        // Stop scanning after a defined period
       // handler.postDelayed({stopScanning()}, 60000)

    } // End of startScanning() function

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScanning() {
        tag = "DeviceScanner.stopScanning()"
        Log.d(tag, "stopScanning called")
        if (!isScanning) return
        isScanning = false
        bluetoothLeScanner?.stopScan(leScanCallback)
    }// End of stopScanning()

    fun getScanState():Boolean{
        return isScanning
    }
} // End of DeviceScanner Class