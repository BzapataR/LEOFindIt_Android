package com.example.leofindit.deviceScanner.presentation.trackerDetails

import android.content.Context

sealed interface TrackerDetailActions {
    data object GoBack : TrackerDetailActions
    data object ToLocateTracker : TrackerDetailActions
    data class EditNickName(val newNickName : String) : TrackerDetailActions
    data class ToManufacturerWebsite(val url : String, val context : Context) : TrackerDetailActions
    data object MarkNeutral : TrackerDetailActions
    data object MarkSafe : TrackerDetailActions
    data object MarkSus : TrackerDetailActions
    data object Copy : TrackerDetailActions
    data class OnIndexChange(val newIndex : Int) : TrackerDetailActions
    data object ShowEditDialog : TrackerDetailActions
    data object ShowDeleteDialog : TrackerDetailActions
    //data class interrogate(val device : BtleDevice)
}