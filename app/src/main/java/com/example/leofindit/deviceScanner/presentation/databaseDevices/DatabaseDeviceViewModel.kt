package com.example.leofindit.deviceScanner.presentation.databaseDevices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leofindit.deviceScanner.data.database.BTLEDeviceDao
import com.example.leofindit.deviceScanner.data.toBtleDeviceList
import com.example.leofindit.deviceScanner.domain.DataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DatabaseDeviceViewModel(
    private val dataRepository: DataRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DatabaseDeviceState())
    val state = _state
        .asStateFlow()
        .onStart {
            observeDevices()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    private var observeSavedDeviceList : Job? = null

    fun onAction(action : DatabaseDevicesActions) {
        when(action) {
            is DatabaseDevicesActions.goBack -> {}
            is DatabaseDevicesActions.onDeviceClicked -> {}
            is DatabaseDevicesActions.OpenBlackList -> {
                _state.update { it.copy(isBlackListOpen = !it.isBlackListOpen) }
            }
            is DatabaseDevicesActions.OpenWhiteList -> {
                _state.update { it.copy(isWhiteListOpen = !it.isWhiteListOpen) }
            }
            is DatabaseDevicesActions.ToggleDeletionDialog -> {
                _state.update { it.copy(displayDeletionRequest = !it.displayDeletionRequest) }
            }
            is DatabaseDevicesActions.DeleteSavedDevices -> {
                viewModelScope.launch { dataRepository.deleteAllDevices() }
            }
        }
    }

    private fun observeDevices () {
        observeSavedDeviceList?.cancel()
            observeSavedDeviceList = combine(
                dataRepository.getWhiteList().distinctUntilChanged(),
                dataRepository.getBlackList().distinctUntilChanged()
        ) { safe, sus ->
            _state.update {
                it.copy(
                    whiteListDevice = safe,
                    blackListDevice = sus
                )
            }
        }.launchIn(viewModelScope)
    }
}