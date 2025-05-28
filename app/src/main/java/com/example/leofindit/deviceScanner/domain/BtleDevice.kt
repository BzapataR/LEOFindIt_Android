package com.example.leofindit.deviceScanner.domain

import android.util.Log


data class BtleDevice(
    val deviceType: String,
    val deviceManufacturer: String,
    val deviceName: String,
    val deviceAddress: String,
    val signalStrength: Int?,
    val isParent: Boolean = false,
    val isTarget: Boolean = false,
    val isSuspicious: Boolean? = null, //True = sus, False = safe, null = neutral
    val isTag: Boolean = false,
    val nickName: String? = null,
    val timeStamp: Long,
    val deviceUuid: List<String>,

    )

