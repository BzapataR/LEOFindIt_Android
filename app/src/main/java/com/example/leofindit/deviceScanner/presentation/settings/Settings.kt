package com.example.leofindit.deviceScanner.presentation.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.leofindit.R
import com.example.leofindit.deviceScanner.presentation.universalComponents.RoundedListItem
import com.example.leofindit.ui.theme.LeoFindItTheme

//********************************************************************************
//                    Settings page, for now just has
//                    Links to Marked Devices and app info
//
//********************************************************************************
@Composable
fun Settings(
    goBack: () -> Unit = {},
    toAppInfo: () -> Unit = {},
    toSavedDevices: () -> Unit = {},
    toThemeSelector : () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Title and close button row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            contentAlignment = Alignment.Center
        ) {

            IconButton(
                onClick = { goBack() },
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.align(alignment = Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.baseline_arrow_back_24),
                    contentDescription = "Back Arrow",
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .shadow(elevation = 24.dp),
        ) {
            RoundedListItem(
                onClick = { toAppInfo() },
                icon = ImageVector.vectorResource(R.drawable.outline_info_24),
                color = Color(0xff00aa00),
                leadingText = "Information & Contact"
            )
            RoundedListItem(
                onClick = { toSavedDevices() },
                icon = ImageVector.vectorResource(R.drawable.baseline_list_24),
                color = Color.Gray,
                leadingText = "Marked Device",
            )
            RoundedListItem(
                onClick = { toThemeSelector() },
                icon = ImageVector.vectorResource(R.drawable.palette_24dp),
                color = Color(0xffaa0000),
                leadingText = "Themes"
            )
        }
    }
}


@Preview
@Composable
fun SettingsPreview() {
    LeoFindItTheme(systemInDark = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Settings()
        }
    }
}