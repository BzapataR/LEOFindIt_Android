package com.example.leofindit.deviceScanner.presentation.trackerDetails

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.leofindit.R
import com.example.leofindit.deviceScanner.presentation.universalComponents.RoundedListItem
import com.example.leofindit.ui.theme.Background
import com.example.leofindit.ui.theme.GoldPrimary
import com.example.leofindit.ui.theme.GoldPrimaryDull
import com.example.leofindit.ui.theme.OnSurface
import com.example.leofindit.ui.theme.Surface
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackerDetailsRoot(
    viewModel : TrackerDetailViewModel = koinViewModel(),
    goBack : () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TrackerDetails(
        state = state,
        onAction = { action ->
            when (action) {
                is TrackerDetailActions.GoBack -> goBack()
                else -> Unit
            }
                viewModel.onAction(action)
        }
    )
}

@Composable
fun TrackerDetails(
    state : TrackerDetailsState,
    onAction : (TrackerDetailActions) -> Unit
) {
    LazyColumn (modifier = Modifier.fillMaxSize()){
        item {
            Spacer(modifier = Modifier.size(12.dp))
            //********************************************************************************
            //                    Device Name, Last Seen, and Safe/Sus Button
            //********************************************************************************

            IconButton(onClick = { onAction(TrackerDetailActions.GoBack) }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.baseline_arrow_back_24),
                    contentDescription = "Back Button",
                    tint = GoldPrimary
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.nickName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
            // Connection status and last seen time
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.error != null) {
                    Log.i("Device Error", state.error)
                    BasicText(
                        text = state.error,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    //todo start scan at start and search scanned list for current device address

                    //Last seen
                    Text(
                        text = "Last seen: ${state.time}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimaryDull,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val options = listOf("Safe", "Neutral", "Suspicious")
                        var selectedIndex = state.indexSelected
                        options.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = options.size
                                ),
                                onClick = {
                                    val selectedIndex = index
                                        when (selectedIndex) {
                                            0 -> {
                                                onAction(TrackerDetailActions.OnIndexChange(0))
                                                onAction(TrackerDetailActions.MarkSafe)
                                                Log.i("Update Device Call", "Device set to safe")
                                            }
                                            1-> {
                                                onAction(TrackerDetailActions.OnIndexChange(1))
                                                state.showDeletionDialog
                                            }

                                            2 -> {
                                                onAction(TrackerDetailActions.OnIndexChange(2))
                                                onAction(TrackerDetailActions.MarkSus)
                                                Log.i(
                                                    "Update Device Call",
                                                    "Device set to suspicious"
                                                )
                                            }
                                    }
                                },
                                selected = index == selectedIndex,
                                colors = SegmentedButtonDefaults.colors(
                                    activeContentColor = GoldPrimary,
                                    activeContainerColor = GoldPrimary.copy(alpha = 0.1f),
                                    activeBorderColor = Color.White.copy(alpha=.25f),
                                    inactiveBorderColor = Color.White.copy(alpha=.25f),
                                    inactiveContainerColor = Surface.copy(alpha =.1f),

                                )
                            ) {
                                Text(text = label)
                            }
                        }
                    }

                    //********************************************************************************
                    //                       Device Address, Manufacturer, and Type
                    //********************************************************************************
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .shadow(elevation = 24.dp),
                    ) {
                        RoundedListItem(
                            leadingText = "Device Address",
                            trailingText = state.address,
                            trailingIcon = ImageVector.vectorResource(R.drawable.sharp_content_copy_24),
                            onClick = {
//                                clipboardManager.setText(AnnotatedString(device.deviceAddress.toString()))
//                                Toast.makeText(
//                                    context,
//                                    "Device address copied",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }
                        )
                        RoundedListItem(
                            leadingText = "Manufacturer",
                            trailingText = "todo add ",
                            trailingIcon = ImageVector.vectorResource(R.drawable.sharp_content_copy_24),
                            onClick = {
//                                clipboardManager.setText(AnnotatedString(device.deviceManufacturer.toString()))
//                                Toast.makeText(
//                                    context,
//                                    "Device manufacturer copied",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }
                        )
                        RoundedListItem(
                            leadingText = "Device type",
                            trailingText = state.deviceType,
                            trailingIcon = ImageVector.vectorResource(R.drawable.sharp_content_copy_24),
                            onClick = {
//                                clipboardManager.setText(AnnotatedString(state.deviceType))
//                                Toast.makeText(
//                                    context,
//                                    "Device Type copied",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }
                        )

                    }

                    //********************************************************************************
                    //                    Locate tracker, ignore tracker, nickname
                    //********************************************************************************
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .shadow(elevation = 24.dp),
                    ) {
                        // list of options
//                    RoundedListItem(
//                        onClick =  {
//                            //crashed on precision finding due to selecting this and device not found
//                            //find device on click here and make fail safe.
//                            viewModel.startScanning(address)
//                            try {
//                                viewModel.findDevice(address)
//                            } catch (_: NoSuchElementException) {
//                                null
//                            }
//                            if (device == null) {
//                                Toast.makeText(context, "Device is out of range", Toast.LENGTH_SHORT).show()
//                                navController?.navigate("Manual Scan") {
//                                    popUpTo(0) { inclusive = true } // This clears back stack
//                                }
//                            }
//                            navController?.navigate(route = "Precision Finding/${address}")
//                        },
//                        color = Color(0xff007aff),
//                        icon = ImageVector.vectorResource(R.drawable.outline_explore_24),
//                        leadingText = "Locate Tracker", trailingText = "Nearby"
//                    )

//                    HorizontalDivider(thickness = Dp.Hairline, color = Color.LightGray)
//
//                    RoundedListItem( //todo if map added add within here
//                        onClick = { navController?.navigate("Observe Tracker") },
//                        color = colorResource(R.color.purple_200),
//                        icon = ImageVector.vectorResource(R.drawable.outline_access_time_24),
//                        leadingText = "Observe Tracker", trailingText = "Off"
//                    )

                        HorizontalDivider(thickness = Dp.Hairline, color = Color.LightGray)

                        RoundedListItem(
                            color = Color.Yellow,
                            icon = Icons.Filled.Create,
                            leadingText = "Create Nickname",
                            trailingText = "Set Nickname",
                            onClick = {
                                onAction(TrackerDetailActions.ShowEditDialog)
                            }
                        )
                    }
                    //
                    Text(
                        text = "Ignoring trackers will stop notifications in the background",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                    //********************************************************************************
                    //                    Manufacturer website
                    //********************************************************************************
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .shadow(elevation = 16.dp),
                    ) {
                        // get database going with from device manufacturer and link with a website
                        // Right now shows generic website to disable device
                        RoundedListItem(
                            onClick = { /*context.startActivity(webIntent)*/ },
                            icon = ImageVector.vectorResource(R.drawable.outline_info_24),
                            color = Color.Green,
                            leadingText = "Manufacture's Website",
                            trailingIcon = ImageVector.vectorResource(R.drawable.baseline_link_24),
                            iconModifier = Modifier.rotate(-45F)
                        )
                    }

                    Text(
                        text = " Learn more, e.g how to disable the tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
    //********************************************************************************
    //                    Nickname dialog Logic
    //********************************************************************************
    if (state.showNickNameDialog) {
        var tempNickName : String = state.nickName
        AlertDialog(
            onDismissRequest = { onAction(TrackerDetailActions.ShowEditDialog) },
            title = {
                Text("Set Nickname", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                TextField(
                    value = tempNickName,
                    onValueChange = { tempNickName = it },
                    placeholder = { Text("Enter nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(TrackerDetailActions.EditNickName(tempNickName))
                        onAction(TrackerDetailActions.ShowEditDialog)
                       // Toast.makeText(context, "Nickname set!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(TrackerDetailActions.ShowEditDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }
        /*
        deletionDialog
         */
    if (state.showDeletionDialog) {
        AlertDialog(
            onDismissRequest = { onAction(TrackerDetailActions.ShowDeleteDialog ) },
            title = {
                Text("Data Deletion", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text("This action will remove the device data from your phones storage")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(TrackerDetailActions.ShowDeleteDialog)
                    }
                ) {
                    Text("Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onAction(TrackerDetailActions.MarkNeutral)
                    //selectedIndex = pendingIndex
                    onAction(TrackerDetailActions.ShowDeleteDialog)
                    //Toast.makeText(context, "Device Deleted", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Delete")
                }

            }
        )
    }
}