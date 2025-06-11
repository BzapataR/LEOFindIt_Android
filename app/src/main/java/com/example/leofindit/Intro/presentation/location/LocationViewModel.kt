package com.example.leofindit.Intro.presentation.location

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocationViewModel(
    private val applicationContext : Context
) : ViewModel() {
    private val _locationState = MutableStateFlow(LocationState())
    val locationState = _locationState.asStateFlow()

    @OptIn(ExperimentalPermissionsApi::class)
    private var accompanistPermissionState : MultiplePermissionsState? = null

    @OptIn(ExperimentalPermissionsApi::class)
    fun setPermissionState(permissionsState : MultiplePermissionsState) {
        _locationState.update {it.copy()}

    }
}