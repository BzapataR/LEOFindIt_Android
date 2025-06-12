//
//  ScanningHomePage.kt
//  LeoFindIt
//
//  Created by Brian Zapata Resendiz
package com.example.leofindit.deviceScanner.presentation.homePage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.leofindit.R
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.deviceScanner.presentation.homePage.components.FAB
import com.example.leofindit.deviceScanner.presentation.homePage.components.MissingPermissions
import com.example.leofindit.deviceScanner.presentation.homePage.components.Scanning
import com.example.leofindit.deviceScanner.presentation.universalComponents.DeviceListEntry
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomePageRoot(
    viewModel: HomePageViewModel = koinViewModel(),
    onDeviceClick: (BtleDevice) -> Unit,
    onSettingsClick: () -> Unit,
    toMarkedDevices: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomePage(
        state = state,
        onAction = { action ->
            when (action) {
                is HomePageActions.onDeviceClick -> onDeviceClick(action.device)
                is HomePageActions.onSettingsButtonClick -> onSettingsClick()
                is HomePageActions.toMarkedDevices -> toMarkedDevices()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun HomePage(state: HomePageState, onAction: (HomePageActions) -> Unit) {
    val lazyListState = rememberLazyListState()
    Box {
        LazyColumn(
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        )
        {
            item { // top Row With Name, Pause/Play, and Settings
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Left: Play/Pause Buttons
                    Row(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = .75f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(8.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        // Stop Button
                        IconButton(
                            onClick = { onAction(HomePageActions.pauseScan) },
                            enabled = !state.missingPermissions,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (!state.isScanning) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.stop_24_fill),
                                contentDescription = null,
                                tint = if (!state.isScanning) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                        }

                        VerticalDivider(color = Color.LightGray, modifier = Modifier.height(24.dp))

                        // Play Button
                        IconButton(
                            onClick = { onAction(HomePageActions.startScan) },
                            enabled = !state.missingPermissions,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (state.isScanning) MaterialTheme.colorScheme.primary else Color.Transparent

                            ),
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.baseline_play_arrow_24),
                                contentDescription = null,
                                tint = if (state.isScanning && (!state.missingPermissions)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

                            )
                        }
                    }

                    // Center: Scan Text
                    Text(
                        text = "Scan",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Right: Settings Icon Button
                    IconButton(
                        onClick = { onAction(HomePageActions.onSettingsButtonClick) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.outline_settings_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (state.isLoading == true || (state.deviceList.isEmpty() && state.isScanning)) {
                item { Scanning { onAction(HomePageActions.toMarkedDevices) } }
            } else
                when {
                    state.missingPermissions == true -> {
                        item {
                            MissingPermissions(stateError = state.error.toString())
                            Log.e("error", state.error.toString())
                        }
                    }

                    state.error != null -> {
                        item {
                            MissingPermissions(stateError = state.error.toString())
                            Log.e("error", state.error.toString())
                        }
                    }

                    else -> {
                        item {
                            if (!state.namedDeviceList.isEmpty()) {
                                Text("Named Devices", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        itemsIndexed(state.namedDeviceList) { index, device ->
                            DeviceListEntry(
                                device = device,
                                onListItemClick = {
                                    onAction(
                                        HomePageActions.onDeviceClick(
                                            device
                                        )
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        item {
                            if (!state.unnamedDevices.isEmpty())
                                Text("Unnamed Devices", color = MaterialTheme.colorScheme.primary)
                        }

                        itemsIndexed(state.unnamedDevices) { index, device ->
                            DeviceListEntry(
                                device = device,
                                onListItemClick = {
                                    onAction(
                                        HomePageActions.onDeviceClick(
                                            device
                                        )
                                    )
                                })
                            Spacer(modifier = Modifier.size(8.dp))
                        }

                    }
                }
        }
        FAB(
            modifier = Modifier.align(Alignment.BottomCenter),
            lazyListState = lazyListState
        )
    }
}


