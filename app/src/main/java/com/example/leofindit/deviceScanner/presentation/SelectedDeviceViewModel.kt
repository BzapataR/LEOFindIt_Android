package com.example.leofindit.deviceScanner.presentation

import androidx.lifecycle.ViewModel
import com.example.leofindit.deviceScanner.domain.BtleDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// global use VM
class SelectedDeviceViewModel : ViewModel() {
    private val _selectedDevice = MutableStateFlow<BtleDevice?>(null)
    val selectedDevice = _selectedDevice.asStateFlow()

    fun onSelectedDevice(device : BtleDevice?) {
        _selectedDevice.value = device
    }
}