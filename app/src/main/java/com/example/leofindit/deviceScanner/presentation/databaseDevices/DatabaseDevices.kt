package com.example.leofindit.deviceScanner.presentation.databaseDevices

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.leofindit.R
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.deviceScanner.presentation.universalComponents.DeviceListEntry
import com.example.leofindit.deviceScanner.presentation.universalComponents.RoundedListItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DatabaseDeviceRoot(
    viewModel: DatabaseDeviceViewModel = koinViewModel(),
    onDeviceClicked : (device: BtleDevice) -> Unit,
    goBack : () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DatabaseDevice(
        state = state,
        onAction = { action ->
            when (action) {
                is DatabaseDevicesActions.goBack -> {
                    goBack()
                }
                is DatabaseDevicesActions.onDeviceClicked -> onDeviceClicked(action.device)

                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun DatabaseDevice(
    onAction : (DatabaseDevicesActions) -> Unit,
    state: DatabaseDeviceState
) {
    val rotationAngle = @Composable fun(bool:Boolean) : Float {
        val animation by animateFloatAsState(
            targetValue = if (bool) 180f else 0f,
            label = "Animation"
        )
        return animation
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp),
                    contentAlignment = Alignment.Center
                ) {

                    IconButton(
                        onClick = { onAction(DatabaseDevicesActions.goBack) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(alignment = Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back Arrow",
                        )
                    }
                    Text(
                        text = "Saved Devices",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            item { //WhiteList

                Row(
                    horizontalArrangement = Arrangement.Center, modifier = Modifier.clickable(
                        onClick = { onAction(DatabaseDevicesActions.OpenWhiteList) }
                    )) {
                    Text(
                        text = "White Listed Devices",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.size(12.dp))
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.baseline_arrow_drop_down_24),
                        contentDescription = "Drop Down",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.rotate(
                            degrees = rotationAngle(state.isWhiteListOpen)
                        )
                    )
//                    RoundedListItem(
//                        leadingText = "White Listed Devices",
//                        trailingIcon = ImageVector.vectorResource(R.drawable.baseline_arrow_drop_down_24),
//                        onClick = { onAction(DatabaseDevicesActions.OpenWhiteList) },
//                        iconModifier = Modifier.rotate(
//                            rotationAngle(state.isWhiteListOpen)
//                        )
//                    )
                }
                AnimatedVisibility(state.isWhiteListOpen) {
                    Column {
                        state.whiteListDevice.forEach { device ->
                            DeviceListEntry(
                                { onAction(DatabaseDevicesActions.onDeviceClicked(device)) },
                                device
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.size(12.dp)) }

            item {// BlackList
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    RoundedListItem(
                        leadingText = "Black Listed Devices",
                        trailingIcon = ImageVector.vectorResource(R.drawable.baseline_arrow_drop_down_24),
                        onClick = { onAction(DatabaseDevicesActions.OpenBlackList) },
                        iconModifier = Modifier.rotate(
                            rotationAngle(state.isBlackListOpen)
                        )
                    )
                    AnimatedVisibility(state.isBlackListOpen) {
                        Column {
                            state.blackListDevice.forEach { device ->
                                DeviceListEntry(
                                    width = LocalConfiguration.current.screenWidthDp.dp,
                                    onListItemClick = {
                                        onAction(
                                            DatabaseDevicesActions.onDeviceClicked(
                                                device
                                            )
                                        )
                                    },
                                    device = device
                                )
                            }
                        }
                    }
                }
            }
        }
        Button(
            onClick = { onAction(DatabaseDevicesActions.ToggleDeletionDialog) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
        ) {
            Text(
                text = "Delete All Saved Devices",
                color = MaterialTheme.colorScheme.onError
            )
        }
        if (state.displayDeletionRequest) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.delete_24),
                        contentDescription = "Delete Icon"
                    )
                },
                onDismissRequest = { onAction(DatabaseDevicesActions.ToggleDeletionDialog ) },
                title = {
                    Text("Data Deletion", style = MaterialTheme.typography.titleLarge)
                },
                text = {
                    Text("This action will remove all the device data from your phone's storage, this is unrecoverable!")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onAction(DatabaseDevicesActions.ToggleDeletionDialog)
                            onAction(DatabaseDevicesActions.DeleteSavedDevices)
                        }
                    ) {
                        Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onAction(DatabaseDevicesActions.ToggleDeletionDialog)
                        //Toast.makeText(, "Device Deleted", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Cancel")
                    }

                }
            )
        }
    }
}

