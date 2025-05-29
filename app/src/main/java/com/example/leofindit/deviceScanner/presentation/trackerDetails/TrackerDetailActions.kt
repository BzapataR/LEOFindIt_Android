package com.example.leofindit.deviceScanner.presentation.trackerDetails

sealed interface TrackerDetailActions {
    data object GoBack : TrackerDetailActions
    data object ToLocateTracker : TrackerDetailActions
    data class EditNickName(val newNickName : String) : TrackerDetailActions
    data object ToManufacturerWebsite : TrackerDetailActions
    data object MarkNeutral : TrackerDetailActions
    data object MarkSafe : TrackerDetailActions
    data object MarkSus : TrackerDetailActions
    data object Copy : TrackerDetailActions
    //data class interrogate(val device : BtleDevice)
}