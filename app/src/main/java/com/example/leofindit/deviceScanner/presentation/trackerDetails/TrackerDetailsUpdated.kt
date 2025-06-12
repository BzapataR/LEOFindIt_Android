package com.example.leofindit.deviceScanner.presentation.trackerDetails

import android.content.ClipData
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.leofindit.R
import com.example.leofindit.deviceScanner.presentation.universalComponents.RoundedListItem
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.core.net.toUri
import com.example.leofindit.ui.theme.LeoFindItTheme
import java.lang.Exception

@Composable
fun TrackerDetailsRoot(
    viewModel : TrackerDetailViewModel = koinViewModel(),
    goBack : () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val localClipboard = LocalClipboard.current
    val context = LocalContext.current
    TrackerDetails(
        state = state,
        onAction = { action ->
            when (action) {
                is TrackerDetailActions.GoBack -> goBack()
                is TrackerDetailActions.Copy -> {
                   val clipData = ClipData.newPlainText("Copy Value", action.CopyVal)
                    viewModel.viewModelScope.launch {localClipboard.setClipEntry(ClipEntry(clipData)) }
                }
                is TrackerDetailActions.DisplayToast -> {
                    Toast.makeText(context, action.DisplayVal, Toast.LENGTH_LONG).show()
                }
                is TrackerDetailActions.ToManufacturerWebsite -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, state.manufacturerSite.toUri())
                        context.startActivity(intent)
                    }
                    catch(_: Exception) {
                        Toast.makeText(context, "Error! Cannot open page", Toast.LENGTH_SHORT).show()
                    }
                }
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
    Log.i("Tracker Details", "Name: ${state.deviceName} Time: ${state.time} isSus: ${state.isSus}")
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.nickName.takeUnless { it == "null" } ?: state.deviceName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary
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
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val options = listOf("Safe", "Neutral", "Suspicious")
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
                                                onAction(TrackerDetailActions.MarkSafe)
                                            }
                                            1-> {
                                                onAction(TrackerDetailActions.ShowDeleteDialog)
                                            }

                                            2 -> {
                                                onAction(TrackerDetailActions.MarkSus)
                                            }
                                    }
                                },
                                selected = index == when(state.isSus) {
                                    false -> 0
                                    null -> 1
                                    true -> 2
                                },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContentColor = MaterialTheme.colorScheme.primary,
                                    activeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    activeBorderColor = Color.White.copy(alpha=.25f),
                                    inactiveBorderColor = Color.White.copy(alpha=.25f),
                                    inactiveContainerColor = MaterialTheme.colorScheme.surface.copy(alpha =.1f),

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
                            onClick = { onAction(TrackerDetailActions.Copy(state.address)) }
                        )
                        RoundedListItem(
                            leadingText = "Manufacturer",
                            trailingText = "todo add ",
                            trailingIcon = ImageVector.vectorResource(R.drawable.sharp_content_copy_24),
                            onClick = {
                                onAction(TrackerDetailActions.Copy("todo value"))
                            }
                        )
                        RoundedListItem(
                            leadingText = "Device type",
                            trailingText = state.deviceType,
                            trailingIcon = ImageVector.vectorResource(R.drawable.sharp_content_copy_24),
                            onClick = { onAction(TrackerDetailActions.Copy(state.deviceType)) }
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
                    RoundedListItem(
                        onClick =  {
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
                        },
                        color = Color(0xff007aff),
                        icon = ImageVector.vectorResource(R.drawable.outline_explore_24),
                        leadingText = "Locate Tracker", trailingText = "Nearby"
                    )

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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                            onClick = { onAction(TrackerDetailActions.ToManufacturerWebsite) },
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
    //********************************************************************************
    //                    Nickname dialog Logic
    //********************************************************************************
    if (state.showNickNameDialog) {
        var tempNickName : String = state.nickName.toString()
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
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.delete_24),
                    contentDescription = "Deletion"
                )
            },
            onDismissRequest = { onAction(TrackerDetailActions.ShowDeleteDialog ) },
            title = {
                Text("Data Deletion", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text("This action will remove the device data from your phones storage")
            },
            confirmButton = {
                TextButton(onClick = {
                    onAction(TrackerDetailActions.MarkNeutral)
                    onAction(TrackerDetailActions.ShowDeleteDialog)
                    //Toast.makeText(, "Device Deleted", Toast.LENGTH_SHORT).show()
                }) {
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onAction(TrackerDetailActions.ShowDeleteDialog)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Preview(
    device = "spec:width=1080px,height=2424px,navigation=buttons", showSystemUi = false, showBackground = true,
    backgroundColor = 0xfffaa
)
@Composable
fun TrackerPreview() {
    LeoFindItTheme {
        TrackerDetails(state = TrackerDetailsState(), onAction = {})
    }
}