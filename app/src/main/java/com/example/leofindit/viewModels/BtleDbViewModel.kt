//package com.example.leofindit.viewModels
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.initializer
//import androidx.lifecycle.viewmodel.viewModelFactory
//import com.example.leofindit.deviceScanner.data.database.AppDatabase
//import com.example.leofindit.deviceScanner.data.database.BTLEDeviceEntity
//import com.example.leofindit.deviceScanner.data.toBtleDevice
//import com.example.leofindit.deviceScanner.data.toEntity
//import com.example.leofindit.deviceScanner.domain.BtleDevice
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//class BtleDbViewModel(private val database: AppDatabase) : ViewModel() {
//    private val _blackList = MutableStateFlow<List<BTLEDeviceEntity>>(emptyList())
//    val blackList: StateFlow<List<BTLEDeviceEntity>> = _blackList.asStateFlow()
//
//    private val _whiteList = MutableStateFlow<List<BTLEDeviceEntity>>(emptyList())
//    val whiteList: StateFlow<List<BTLEDeviceEntity>> = _whiteList.asStateFlow()
//
//    init {
//        loadData()
//    }
//
//     fun loadData() {
//        viewModelScope.launch {
//            _blackList.value = database.btleDeviceDao().getSusDevices()
//            _whiteList.value = database.btleDeviceDao().getSafeDevices()
//        }
//    }
//    fun addDevice(device: BtleDevice) {
//        viewModelScope.launch {
//            database.btleDeviceDao().insert(device.toEntity())
//        }
//    }
//    fun deleteDevice(device: BtleDevice) {
//        viewModelScope.launch {
//            database.btleDeviceDao().deleteDeviceByAddress(deviceAddress = device.deviceAddress.toString())
//        }
//    }
//    fun getDatabase() : AppDatabase {
//        return database
//    }
//    suspend fun findDevice(address:String) : BtleDevice? {
//        var device = database.btleDeviceDao().getDeviceByAddress(address)?.toBtleDevice()
//        Log.i("DB device" ,"$device")
//        return device
//    }
//
//    companion object {
//        fun provideFactory(
//            database: AppDatabase,
//        ): ViewModelProvider.Factory = viewModelFactory {
//            initializer {
//                BtleDbViewModel(database = database)
//            }
//        }
//    }
//}
