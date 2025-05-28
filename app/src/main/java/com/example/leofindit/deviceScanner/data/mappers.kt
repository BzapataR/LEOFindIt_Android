package com.example.leofindit.deviceScanner.data

import com.example.leofindit.deviceScanner.data.database.BTLEDeviceEntity
import com.example.leofindit.deviceScanner.domain.BtleDevice


fun BtleDevice.toEntity(): BTLEDeviceEntity {
    return BTLEDeviceEntity(
        deviceAddress = this.deviceAddress.toString(),
        deviceManufacturer = this.deviceManufacturer,
        deviceName = this.deviceName,
        deviceType = this.deviceType,
        isSuspicious = this.isSuspicious,
        deviceNickname = this.nickName.toString(),
        timestamp = listOf(this.timeStamp),
        UUID = this.deviceUuid,
        rssi = this.signalStrength ?: -999
    )
}
fun BTLEDeviceEntity.toBtleDevice(): BtleDevice {
    return BtleDevice(
        deviceType = this.deviceType,
        deviceManufacturer = this.deviceManufacturer,
        deviceName = this.deviceName,
        deviceAddress = this.deviceAddress,
        signalStrength = this.rssi,          // rssi will get last record
        isParent = false,              // isParent is set to false by default
        isTarget = false,              // isTarget is set to false by default
        isSuspicious = this.isSuspicious, // Transfer from BTLEDeviceEntity
        isTag = false,                 // isTag is set to false by default
        nickName = this.deviceNickname,
        timeStamp = this.timestamp.first(), // Use the first timestamp value, default to 0L if empty
        deviceUuid = this.UUID
    )
}
// Extension function to convert a list of BTLEDeviceEntity to a list of BtleDevice
fun List<BTLEDeviceEntity>.toBtleDeviceList(): List<BtleDevice> {
    return this.map { it.toBtleDevice() }
}