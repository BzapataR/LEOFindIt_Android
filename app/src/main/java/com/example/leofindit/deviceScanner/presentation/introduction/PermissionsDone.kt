package com.example.leofindit.deviceScanner.presentation.introduction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leofindit.R
import com.example.leofindit.ui.theme.GoldPrimary
import com.example.leofindit.ui.theme.GoldPrimaryDull
import com.example.leofindit.ui.theme.LeoFindItTheme
import com.example.leofindit.ui.theme.OnSurface
import com.example.leofindit.ui.theme.Surface
/*********************************************************************************
 *                  When all into pages are complete
 *********************************************************************************/
@Composable
fun PermissionsDone(onFinish: (() -> Unit)? = null) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "All Done!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = GoldPrimary
        )
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_check_circle_24),
            contentDescription = "Check Mark",
            tint = GoldPrimaryDull,
            modifier = Modifier.size(100.dp)
        )
        Column {
            Text(
                text = "Important: Background scanning does not work with AirTags and \"Find My\"" +
                        "devices. Protection from these trackers is already provided by Apple's" +
                        "pre-installed \"Find My\" app",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                color = GoldPrimary
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "For technical reasons, not all features are available for every tracker " +
                        "type. For example, the determination if trackers are separated from " +
                        "their owners is only available for SmartTags.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                color = GoldPrimary
            )
        }
        Button(
            onClick = {
                if (onFinish != null) {
                    onFinish()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = OnSurface),
            modifier = Modifier.fillMaxWidth(.75f)

        ) {
            Text(
                text = "Continue"
            )
        }
    }

}

@Preview
@Composable
fun PermissionsDonePreview() {
    LeoFindItTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            PermissionsDone()
        }
    }
}