package com.example.leofindit.model

import android.util.Log


data class BtleDevice(
    val deviceType: String,
    val deviceManufacturer: String,
    val deviceName: String,
    val deviceAddress: String?,
    var signalStrength: Int?,
    val isParent: Boolean,
    var isTarget: Boolean,
    private var isSuspicious: Boolean?, //True = sus, False = safe, null = neutral
    val isTag: Boolean,
    private var nickName: String,
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
    fun setAsSafe () {
        isSuspicious = false
    }
    fun setAsNeutral() {
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
        Log.i(lTag, "${getNickName()} ($deviceType) marked as safe.")
    }

    fun markSuspicious(){
        setAsSuspicious()
        Log.i(lTag, "${getNickName()} ($deviceType) marked as suspicous.")
    }

    fun markNeutral(){
        setAsNeutral()
        Log.i(lTag, "${getNickName()} ($deviceType) marked as unknown.")
    }

}

