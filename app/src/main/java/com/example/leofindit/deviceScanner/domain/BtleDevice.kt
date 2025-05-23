package com.example.leofindit.deviceScanner.domain

import android.util.Log


data class BtleDevice(
    var deviceType: String,
    val deviceManufacturer: String,
    var deviceName: String,
    val deviceAddress: String,
    var signalStrength: Int?,
    val isParent: Boolean = false,
    var isTarget: Boolean = false,
    var isSuspicious: Boolean? = null, //True = sus, False = safe, null = neutral
    val isTag: Boolean = false,
    var nickName: String? = null,
    val timeStamp: Long,
    var deviceUuid: List<String>,

    ){

    private var lTag: String = "BTLEDevice"

    private fun setAsSuspicious(){
        isSuspicious = true
    }
    fun getIsSuspicious():Boolean?{
        return isSuspicious
    }
    private fun setAsSafe () {
        isSuspicious = false
    }
    private fun setAsNeutral() {
        isSuspicious = null
    }
    fun setNickName(newNickName: String) {
        var oldNickName = getNickName()
        nickName = newNickName
        Log.i(lTag, "User renamed $deviceType: $oldNickName to $newNickName.")
    }

    fun getNickName(): String? {
        return nickName
    }

    fun markSafe(){
        setAsSafe()
        Log.i(lTag, "($deviceType) marked as safe.")
    }

    fun markSuspicious(){
        setAsSuspicious()
        Log.i(lTag, " ($deviceType) marked as suspicous.")
    }

    fun markNeutral(){
        setAsNeutral()
        Log.i(lTag, "($deviceType) marked as unknown.")
    }

}

