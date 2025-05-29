package com.example.leofindit.deviceScanner.presentation.homePage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leofindit.deviceScanner.domain.DataRepository
import com.example.leofindit.deviceScanner.domain.isUnNamed
import com.example.leofindit.errors.onError
import com.example.leofindit.errors.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomePageViewModel(
    private val dataRepository: DataRepository
) : ViewModel() {

    private var observeDeviceListJob : Job? = null

    private val _state = MutableStateFlow(HomePageState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: HomePageActions) {
        when (action) {
            is HomePageActions.onDeviceClick -> { /* pass logic from main activity to navigate*/}
            is HomePageActions.onSettingsButtonClick -> {/* pass logic from main activity to navigate*/}
            is HomePageActions.toMarkedDevices -> {/* pass logic from main activity to navigate*/}

            is HomePageActions.pauseScan -> {
                pauseScan()
            }

            is HomePageActions.startScan -> {
                startScan()
            }
        }
    }
    private fun observeDeviceList () {
        observeDeviceListJob?.cancel()
        dataRepository.startScanning().onSuccess { resultFlow ->
            observeDeviceListJob = resultFlow.onEach { deviceList ->
                val (unnamed, named) = deviceList
                    .sortedByDescending{ it.signalStrength }
                    .partition{ it.isUnNamed() }
                Log.i("status", "unnamed size: ${unnamed.size} named: ${named.size}")
                _state.update {
                    it.copy(
                        deviceList = deviceList,
                        namedDeviceList = named,
                        unnamedDevices = unnamed,
                        isLoading = false,
                        error = null,
                    )
                }
            } .launchIn(viewModelScope)
        }
            .onError { error ->
                pauseScan()
                _state.update{
                    it.copy(
                        isScanning = false,
                        namedDeviceList = emptyList(),
                        unnamedDevices = emptyList(),
                        isLoading = false,
                        error = error.toString()
                    )
                }
            }
    }

    private fun startScan() {
        if (_state.value.isScanning) {
            return
        }
        _state.update { it.copy(isLoading = true, error = null, isScanning = true) }
        observeDeviceList()
        Log.i("status", "${_state.value.isScanning}")
    }

    private fun pauseScan() {
        if (!_state.value.isScanning) return
        observeDeviceListJob?.cancel()
        dataRepository.stopScanning().onSuccess {
            _state.update {
                it.copy(
                    isScanning = false,
                    isLoading = false,
                    error = null,
                )
            }
        }
            .onError { error ->
                _state.update{
                    it.copy(
                        isScanning = false,
                        isLoading= false,
                        error = error.toString()
                    )
                }
            }
        Log.i("status", "${_state.value.isScanning}")

    }
}