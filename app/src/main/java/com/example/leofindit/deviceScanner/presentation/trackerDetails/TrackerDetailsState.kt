package com.example.leofindit.deviceScanner.presentation.trackerDetails

data class TrackerDetailsState (
    val deviceName : String = "",
    val time : String = "",
    val address : String = "",
    val deviceType : String = "",
    val nickName : String? = null,
    val manufacturerSite : String = "https://www.google.com", // todo replace with correct site
    val manufacturer : String = "",
    val manufacturerData : String= "",
    val isSus : Boolean? = null,
    val isLoading : Boolean = true,
    val error : String? = null,
    val showDeletionDialog : Boolean = false,
    val showNickNameDialog: Boolean = false,
    val isInterrogating : Boolean = false

)