package com.example.leofindit.deviceScanner.presentation.trackerDetails

data class TrackerDetailsState (
    val deviceName : String = "",
    val time : String = "",
    val address : String = "",
    val deviceType : String = "",
    val nickName : String = "",
    val manufacturerSite : String = "",
    val isSus : Boolean? = null,
    val isLoading : Boolean = true,
)