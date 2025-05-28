package com.example.leofindit.deviceScanner.presentation.trackerDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import androidx.lifecycle.viewModelScope
import com.example.leofindit.deviceScanner.data.DeviceRepository
import com.example.leofindit.navigation.MainNavigation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TrackerDetailViewModel (
    private val deviceRepository: DeviceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var observeDevice : Job? = null
    private val deviceAddress = savedStateHandle.toRoute<MainNavigation.TrackerDetails>().address
    private val _state = MutableStateFlow(TrackerDetailsState())
    val state = _state.asStateFlow()
        .onStart {
            fetchDevice()
            observeMarkedStatus()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    private fun observeMarkedStatus() {
       deviceRepository.getDeviceByAddress(deviceAddress)
    }
//    fun onAction(action: TrackerDetailActions) {
//        when(action) {
//            TrackerDetailActions.copy -> TODO()
//            TrackerDetailActions.editNickName -> deviceRepository.
//            TrackerDetailActions.goBack -> TODO()
//            TrackerDetailActions.markNeutral -> TODO()
//            TrackerDetailActions.markSafe -> TODO()
//            TrackerDetailActions.markSus -> TODO()
//            TrackerDetailActions.toLocateTracker -> TODO()
//            TrackerDetailActions.toManufactuerWebsite -> TODO()
//        }
//    }

    private fun fetchDevice() {
        observeDevice?.cancel()
        _state.update{it.copy(isLoading = true)}
        observeDevice = deviceRepository.getDeviceByAddress(deviceAddress).onEach { device ->
            _state.update {
                it.copy(
                    deviceName = device?.deviceName.toString(),
                    time = device?.timeStamp.toString(),
                    address = deviceAddress,
                    deviceType = "BluetoothDevice",//TODO this is temp
                    nickName = device?.nickName.toString(),
                    manufacturerSite = "www.google.com", // TODO temp
                    isSus = device?.isSuspicious,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)

    }

}