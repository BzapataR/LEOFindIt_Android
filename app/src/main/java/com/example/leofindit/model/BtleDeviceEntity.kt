package com.example.leofindit.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
//Building of the data base table
@Entity(tableName = "btle_devices")
data class BTLEDeviceEntity(
    @PrimaryKey val deviceAddress: String,
    @ColumnInfo(name = "Device Manufacture") val deviceManufacturer: String,
    @ColumnInfo(name = "Device Name") val deviceName: String,
    @ColumnInfo(name = "Device Type") val deviceType: String,
    @ColumnInfo(name = "is Suspicious") val isSuspicious: Boolean?,
    @ColumnInfo(name = "Device Nickname") val deviceNickname: String,
    @ColumnInfo(name = "Time Stamp") val timestamp: List<Long>,
    @ColumnInfo(name = "UUID") val UUID: String,
    @ColumnInfo(name = "Last RSSI") val rssi: Int,

    )

fun BtleDevice.toEntity(): BTLEDeviceEntity {
    // Skip if deviceAddress is null

    return BTLEDeviceEntity(
        deviceAddress = this.deviceAddress.toString(),
        deviceManufacturer = this.deviceManufacturer,
        deviceName = this.deviceName,
        deviceType = this.deviceType,
        isSuspicious = this.getIsSuspicious(),
        deviceNickname = this.getNickName().toString(),
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
        signalStrength = this.rssi,          // signalStrength is set to null
        isParent = false,              // isParent is set to false by default
        isTarget = false,              // isTarget is set to false by default
        isSuspicious = this.isSuspicious, // Transfer from BTLEDeviceEntity
        isTag = false,                 // isTag is set to false by default
        nickName = this.deviceNickname,
        timeStamp = this.timestamp.firstOrNull() ?: 0L, // Use the first timestamp value, default to 0L if empty
        deviceUuid = this.UUID
    )
}
// Extension function to convert a list of BTLEDeviceEntity to a list of BtleDevice
fun List<BTLEDeviceEntity>.toBtleDeviceList(): List<BtleDevice> {
    return this.map { it.toBtleDevice() }
}