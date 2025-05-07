package com.example.leofindit.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.leofindit.model.BtleDevice
import com.example.leofindit.ui.theme.GoldPrimaryDull
import com.example.leofindit.ui.theme.LeoIcons
import com.example.leofindit.ui.theme.Surface

//********************************************************************************
//                    The Card that is used for displaying in
//                    Scanning Home page
//********************************************************************************
@Composable
fun DeviceListEntry(navController: NavController? = null, device : BtleDevice) {
    val address = device.deviceAddress
    Card(
        modifier = Modifier.size(width = 360.dp, height = 40.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        onClick = {
            navController?.navigate(route ="Tracker Details/$address")
        }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left-side content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = LeoIcons.Bluetooth, // Change once database of different device types
                    contentDescription = "Device Type Icon",
                    tint = Color.Unspecified
                )
                    Text(
                        text = device.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(192.dp),
                        color = GoldPrimaryDull
                    )
            }
            Row (horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                // Signal strength icon aligned to the end
                val signalStrengthIcon = when {
                    device.signalStrength!! >= -50 -> LeoIcons.SignalStrengthHigh
                    device.signalStrength!! >= -70 -> LeoIcons.SignalStrengthMed
                    else -> LeoIcons.SignalStrengthLow
                }
                //Signal Color
                val signalStrengthColor = when {
                    device.signalStrength!! >= -50 -> Color.Green
                    device.signalStrength!! >= -70 -> Color.Yellow
                    else -> Color.Red
                }
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Icon(
                        imageVector = signalStrengthIcon,
                        contentDescription = "Signal Strength",
                        tint = signalStrengthColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "${device.signalStrength}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)

                        )
                    }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Signal Strength",
                    tint = Color.LightGray,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
@Composable
fun PreviousDeviceListEntry(navController: NavController?, device : BtleDevice) {
    val address = device.deviceAddress
    Card(
        modifier = Modifier.size(width = 360.dp, height = 40.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        onClick = {
            navController?.navigate(route ="Device From Db/$address")
        }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left-side content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = LeoIcons.Bluetooth, // Change once database of different device types
                    contentDescription = "Device Type Icon",
                    tint = Color.Unspecified
                )
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(192.dp),
                    color = GoldPrimaryDull
                )
            }
            Row (horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Signal Strength",
                    tint = Color.LightGray,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
@Preview
@Composable
fun DeviceDetailEntryPreview() {
    val device1 = BtleDevice(deviceType = "", deviceUuid = "", deviceManufacturer = "", deviceAddress = "", deviceName = "Device 1", isSuspicious = false, isTag = false, isParent = false, isTarget = false, nickName = "", timeStamp = 0L, signalStrength = -90)
    val device2 = BtleDevice(deviceType = "", deviceUuid = "", deviceManufacturer = "", deviceAddress = "", deviceName = "Device 2", isSuspicious = false, isTag = false, isParent = false, isTarget = false, nickName = "", timeStamp = 0L, signalStrength = -60)
    val device3 = BtleDevice(deviceType = "", deviceUuid = "", deviceManufacturer = "", deviceAddress = "", deviceName = "Device 3", isSuspicious = false, isTag = false, isParent = false, isTarget = false, nickName = "", timeStamp = 0L, signalStrength = 0)
    Column(
        Modifier.padding(12.dp)
    ) {
        DeviceListEntry(device = device1)
        DeviceListEntry(device = device2)
        DeviceListEntry(device = device3)
    }
}