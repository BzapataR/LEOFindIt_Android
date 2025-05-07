package com.example.leofindit.model

import android.util.Log


data class BtleDevice(
    val deviceType: String,
    val deviceManufacturer: String,
    val deviceName: String,
    val deviceAddress: String?,
    var signalStrength: Int?,
    val isParent: Boolean = false,
    var isTarget: Boolean = false,
    private var isSuspicious: Boolean? = null, //True = sus, False = safe, null = neutral
    val isTag: Boolean = false,
    private var nickName: String? = null,
    val timeStamp: Long,
    var deviceUuid: String,

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

