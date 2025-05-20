package com.example.leofindit.deviceScanner.data.database

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
    @ColumnInfo(name = "UUID") val UUID: List<String>,
    @ColumnInfo(name = "Last RSSI") val rssi: Int,

    )