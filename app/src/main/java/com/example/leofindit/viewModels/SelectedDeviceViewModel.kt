package com.example.leofindit.viewModels

import androidx.lifecycle.ViewModel
import com.example.leofindit.deviceScanner.domain.BtleDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectedDeviceViewModel : ViewModel() {
    private val _selectedDevice = MutableStateFlow<BtleDevice?>(null)
    val selectedDevice = _selectedDevice.asStateFlow()

    fun onSelectedDevice(btleDevice: BtleDevice?) {
        _selectedDevice.value = btleDevice
    }
}