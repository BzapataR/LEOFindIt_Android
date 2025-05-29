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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    fun onAction(action: TrackerDetailActions) {
        when(action) {
            is TrackerDetailActions.Copy -> {}
            is TrackerDetailActions.EditNickName -> {
                editNickName(address = _state.value.address, newNickName = action.newNickName)
            }
            is TrackerDetailActions.GoBack -> {/* pass logic from main activity to pop backStack*/}
            is TrackerDetailActions.MarkNeutral -> { markNeutral() }
            is TrackerDetailActions.MarkSafe -> { markSafe() }
            is TrackerDetailActions.MarkSus -> { markSus() }
            is TrackerDetailActions.ToLocateTracker -> {/* pass logic from main activity to pop backStack*/}
            is TrackerDetailActions.ToManufacturerWebsite -> {}
        }
    }

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
    private fun markSus () {
        viewModelScope.launch {
            deviceRepository.editDeviceSus(address = _state.value.address, newSusValue = true)
        }
    }
    private fun markSafe () {
        viewModelScope.launch {
            deviceRepository.editDeviceSus(address = _state.value.address, newSusValue = false)
        }
    }
    private fun markNeutral () {
        viewModelScope.launch {
            deviceRepository.editDeviceSus(address = _state.value.address, newSusValue = null)
        }
    }
    private fun editNickName(address : String, newNickName : String) {
        viewModelScope.launch {
            deviceRepository.editNickName(address = address, newNickName = newNickName)
        }
    }

}