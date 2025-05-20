package com.example.leofindit.deviceScanner.presentation.settings


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.leofindit.R
import com.example.leofindit.deviceScanner.domain.BtleDevice
import com.example.leofindit.deviceScanner.presentation.components.RoundedListItem
import com.example.leofindit.ui.theme.GoldPrimary
import com.example.leofindit.ui.theme.GoldPrimaryDull
import com.example.leofindit.ui.theme.OnSurface
import com.example.leofindit.viewModels.BtleDbViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
@Composable
fun DeviceByDb(
    navController: NavController? = null,
    address: String,
    dbViewModel: BtleDbViewModel,
) {
    val deviceState by produceState<BtleDevice?>(
        initialValue = null,
        key1 = address
    ) {
        value = dbViewModel.findDevice(address)
        Log.i("Composable Device", "Loaded from DB: $value")
    }
    //Handle the null state, we need to wait until the device is loaded
    if(deviceState == null){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var device = deviceState!!
    var ignoreTracker by remember { mutableStateOf(device.getIsSuspicious() == null) }
    var showDialog by remember { mutableStateOf(false) }
    var showDeletionDialog by remember { mutableStateOf(false)}
    var nickname by remember { mutableStateOf(device.getNickName()?.takeIf { it != "null" } ?: "") }
// Time vars
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }
    var pendingIndex by remember { mutableIntStateOf(-1) }
    val deviceTimeStamp = rememberUpdatedState(newValue = device.timeStamp)
    val timeDiffMillis = currentTime - deviceTimeStamp.value
    val hours = TimeUnit.MILLISECONDS.toHours(timeDiffMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis) - TimeUnit.HOURS.toMinutes(hours)
    val seconds =
        TimeUnit.MILLISECONDS.toSeconds(timeDiffMillis) - TimeUnit.MINUTES.toSeconds(minutes)
    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
// todo remove this or implement a version using a device variable
    val notCurrentlyReachable = false
// context vars
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    //todo make a database of manufactures with their website to remove/disable device
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        "https://www.deccanherald.com/technology/gadgets/how-to-detect-and-disable-illegal-bluetooth-trackers-on-android-phones-3323302".toUri()
        // "https://support.thetileapp.com/hc/en-us/articles/360037001854-Disconnect-a-Partner-Device-from-My-Tile-Account#:~:text=During%20this%20process%2C%20the%20device,back%20to%20your%20Tile%20account.".toUri()
    )
// device marking vars
    var selectedIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(device) {
        selectedIndex = when (device.getIsSuspicious()) {
            null -> -1
            false -> 0
            true -> 1
        }
    }



    LazyColumn {
        item {
            Spacer(modifier = Modifier.size(56.dp))
            //********************************************************************************
            //                    Device Name, Last Seen, and Safe/Sus Button
            //********************************************************************************

            IconButton(onClick = { navController?.popBackStack() }) {
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
                    text = device.getNickName().takeIf { it != ("null") } ?: device.deviceName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
            // Connection status and last seen time
            val connectionStatus = true
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (notCurrentlyReachable == true) {
                    BasicText(
                        text = "No Connection",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    //todo start scan at start and search scanned list for current device address
                    if (connectionStatus == null) {
                        Text(
                            text = "connectionStatus.description",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                //Last seen
                Text(
                    text = "Last seen: $formattedTime",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimaryDull,
                    modifier = Modifier.fillMaxWidth()
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .width(270.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    val options = listOf("Mark Safe", "Mark Suspicious")
                    options.forEachIndexed { index, label ->
                        var isSelected = index == selectedIndex
                        val backgroundColor = if (index == 0) Color.LightGray else Color.Black
                        val containerColor = if (index == 0) Color.Black else Color.LightGray
                        val inactiveBackgroundColor =
                            if (index == 0) Color.LightGray else Color.Black
                        val inactiveContentColor = if (index == 0) Color.Black else Color.LightGray

                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = {
                                val newIndex = if (isSelected) -1 else index
                                if (newIndex == -1) {
                                    pendingIndex = newIndex
                                    showDeletionDialog = true
                                } else {
                                    selectedIndex = newIndex
                                    when (newIndex) {
                                        0 -> {
                                            device.markSafe()
                                            dbViewModel.addDevice(device)
                                            Log.i("Update Device Call", "Device set to safe")
                                        }
                                        1 -> {
                                            device.markSuspicious()
                                            dbViewModel.addDevice(device)
                                            Log.i("Update Device Call", "Device set to suspicious")
                                        }
                                    }
                                }
                            }
                            ,
                            selected = index == selectedIndex,
                            enabled = true,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = backgroundColor,
                                activeContentColor = containerColor,
                                inactiveContainerColor = inactiveBackgroundColor,
                                inactiveContentColor = inactiveContentColor,
                            ),
                        ) {
                            Text(label)
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
                        trailingText = device.deviceAddress ?: "Unknown",
                        trailingIcon = ImageVector.vectorResource(R.drawable.sharp_content_copy_24),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(device.deviceAddress.toString()))
                            Toast.makeText(
                                context,
                                "Device address copied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    RoundedListItem(
                        leadingText = "Manufacturer",
                        trailingText = device.deviceManufacturer,
                        trailingIcon = ImageVector.vectorResource(R.drawable.sharp_content_copy_24),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(device.deviceManufacturer.toString()))
                            Toast.makeText(
                                context,
                                "Device manufacturer copied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    RoundedListItem(
                        leadingText = "Device type",
                        trailingText = device.deviceType,
                        trailingIcon = ImageVector.vectorResource(R.drawable.sharp_content_copy_24),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(device.deviceType.toString()))
                            Toast.makeText(
                                context,
                                "Device Type copied",
                                Toast.LENGTH_SHORT
                            ).show()
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
                        color = Color.Red,
                        icon = ImageVector.vectorResource(R.drawable.outline_not_interested_24),
                        leadingText = "Ignore Tracker",
                        customTrailingContent = {
                            Switch(
                                colors = SwitchDefaults.colors(checkedTrackColor = GoldPrimary),
                                checked = selectedIndex == -1,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) showDeletionDialog = true
                                },
                            )
                        }
                    )

                    HorizontalDivider(thickness = Dp.Hairline, color = Color.LightGray)

                    RoundedListItem(
                        color = Color.Yellow,
                        icon = Icons.Filled.Create,
                        leadingText = "Create Nickname",
                        trailingText = "Set Nickname",
                        onClick = {
                            showDialog = true
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
                        onClick = { context.startActivity(webIntent) },
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
    //********************************************************************************
    //                    Nickname dialog Logic
    //********************************************************************************
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("Set Nickname", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                OutlinedTextField(
                    value = nickname.toString(),
                    onValueChange = { nickname = it },
                    placeholder = { Text("Enter nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        device.setNickName(nickname)
                        showDialog = false
                        Toast.makeText(context, "Nickname set!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showDeletionDialog) {
        AlertDialog(
            onDismissRequest = { showDeletionDialog = false },
            title = {
                Text("Data Deletion", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text("This action will remove the device data from your phones storage")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeletionDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dbViewModel.deleteDevice(device)
                    device.markNeutral()
                    selectedIndex = pendingIndex
                    ignoreTracker = true
                    showDeletionDialog = false
                    Toast.makeText(context, "Device Deleted", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Delete")
                }

            }
        )
    }
}
