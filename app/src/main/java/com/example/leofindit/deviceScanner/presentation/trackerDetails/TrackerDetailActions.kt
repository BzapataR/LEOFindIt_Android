package com.example.leofindit.deviceScanner.presentation.trackerDetails

import com.example.leofindit.deviceScanner.domain.BtleDevice

sealed interface TrackerDetailActions {
    data object goBack : TrackerDetailActions
    data object toLocateTracker : TrackerDetailActions
    data object editNickName : TrackerDetailActions
    data object toManufactuerWebsite : TrackerDetailActions
    data object markNeutral : TrackerDetailActions
    data object markSafe : TrackerDetailActions
    data object markSus : TrackerDetailActions
    data object copy : TrackerDetailActions
    //data class interrogate(val device : BtleDevice)
}