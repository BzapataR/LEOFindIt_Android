package com.example.leofindit.Intro.presentation.location

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leofindit.controller.LocationHelper
import com.example.leofindit.controller.LocationHelper.checkingLocationEnabledState
import com.example.leofindit.controller.LocationHelper.rememberLocationPermissionState
import com.example.leofindit.R
import com.example.leofindit.ui.theme.LeoFindItTheme

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted

/*********************************************************************************
 *                   Location init page
 *********************************************************************************/
@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun LocationAccess(toNextComposable : ()-> Unit) {

    val context = LocalContext.current
    val permissionsState = rememberLocationPermissionState()
    val isLocationOn by checkingLocationEnabledState()

    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        val uri = Uri.fromParts("package",context.packageName, null )
        this.data = uri
    }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize().padding(12.dp)
    ){
        Text(
            text = "Location Access",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_location_pin_24),
            contentDescription = "Location Pin",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(100.dp)
        )
        Text(
            text = "We use the location of your device to determine if a tracker is following" +
                    " you. All location data stays on device. Please tap \"Continue\" and" +
                    " select \"Allow While Using App\". Make sure \"Precise\" is turned on.  ",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start= 8.dp, end = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Button(
            onClick = {
                when {
                    !permissionsState.allPermissionsGranted -> {
                        if (!permissionsState.shouldShowRationale) {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                        else {
                            Toast.makeText(context, "Permission denied, please go into settings to enable " +
                                    "permissions", Toast.LENGTH_LONG).show()
                            context.startActivity(intent)
                        }
                    }
                    !isLocationOn -> {
                        LocationHelper.enableLocationService(context)
                    }
                   permissionsState.allPermissionsGranted && LocationHelper.isLocationServiceEnabled() -> {
                       toNextComposable()
                   }
                }
            },
            modifier = Modifier.fillMaxWidth(.75f),
        ) {
               when {
                   !permissionsState.permissions.first { state ->
                       state.permission == Manifest.permission.ACCESS_FINE_LOCATION
                   }
                       .status.isGranted -> Text("Please Grant Precise Location")

                   !permissionsState.allPermissionsGranted -> Text("Request Permission")

                    !isLocationOn -> Text("Turn on Location")

                    permissionsState.allPermissionsGranted && LocationHelper.isLocationServiceEnabled() ->
                        Text("Continue")
                   else -> {"error"}
               }
        }
    }
}
@Preview
@Composable
fun LocationAccessPreview() {
    LeoFindItTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LocationAccess({})
        }
    }
}