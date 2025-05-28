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
import com.example.leofindit.deviceScanner.presentation.homePage.components.MissingPermissions
import com.example.leofindit.deviceScanner.presentation.homePage.components.Scanning
import com.example.leofindit.deviceScanner.presentation.universalComponents.DeviceListEntry
import com.example.leofindit.ui.theme.GoldPrimary
import com.example.leofindit.ui.theme.InversePrimary
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
    LazyColumn(
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
                        .background(InversePrimary, shape = MaterialTheme.shapes.medium)
                        .padding(8.dp)
                        .align(Alignment.CenterStart)
                ) {
                    // Stop Button
                    IconButton(
                        onClick = { onAction(HomePageActions.pauseScan) },
                        enabled = !state.missingPermissions,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (!state.isScanning) GoldPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.stop_24dp_e3e3e3_fill1_wght400_grad0_opsz24),
                            contentDescription = null,
                            tint = if (!state.isScanning) Color.Black else GoldPrimary
                        )
                    }

                    VerticalDivider(color = Color.LightGray, modifier = Modifier.height(24.dp))

                    // Play Button
                    IconButton(
                        onClick = { onAction(HomePageActions.startScan) },
                        enabled = !state.missingPermissions,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (state.isScanning) GoldPrimary else Color.Transparent

                        ),
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_play_arrow_24),
                            contentDescription = null,
                            tint = if (state.isScanning && (!state.missingPermissions)) Color.Black else GoldPrimary

                        )
                    }
                }

                // Center: Scan Text
                Text(
                    text = "Scan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
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
                        tint = GoldPrimary
                    )
                }
            }
        }
        if (state.isLoading == true || (state.deviceList.isEmpty() && state.isScanning)) {
            item { Scanning { onAction(HomePageActions.toMarkedDevices) } }
        }
        else
            when {
                state.missingPermissions == true -> { item {
                    MissingPermissions(stateError = state.error.toString())
                    Log.e("error", state.error.toString())
                }}
                state.error != null -> { item {
                    MissingPermissions(stateError = state.error.toString())
                    Log.e("error", state.error.toString())
                } }
                else -> {
                    item{
                        if (!state.namedDeviceList.isEmpty()) {
                            Text("Named Devices", color = GoldPrimary)
                        }
                    }
                    itemsIndexed(state.namedDeviceList) { index, device ->
                        DeviceListEntry(device = device, onListItemClick = { onAction(HomePageActions.onDeviceClick(device)) })
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    item{
                        if (!state.unnamedDevices.isEmpty())
                            Text("Unnamed Devices", color= GoldPrimary)
                    }

                    itemsIndexed(state.unnamedDevices) { index, device ->
                        DeviceListEntry(device = device, onListItemClick = { onAction(HomePageActions.onDeviceClick(device))})
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                }
            }
    }
}


