package com.example.leofindit.deviceScanner.presentation.trackerDetails

import android.content.Intent
import androidx.core.net.toUri

data class TrackerDetailsState (
    val deviceName : String = "",
    val time : String = "",
    val address : String = "",
    val deviceType : String = "",
    val nickName : String? = null,
    val manufacturerSite : String = "https://www.google.com", // todo replace with correct site
    val isSus : Boolean? = null,
    val isLoading : Boolean = true,
    val error : String? = null,
    val showDeletionDialog : Boolean = false,
    val showNickNameDialog: Boolean = false

)