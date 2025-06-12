package com.example.leofindit.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.dataStoreFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.leofindit.di.appSettingsDataStore
import com.example.leofindit.ui.theme.LeoFindItTheme
import kotlinx.coroutines.launch

@Composable
fun ThemeSelector(userPreference: UserPreferencesRepository, goBack : () -> Unit = {}, currentTheme: ThemePreference) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    LaunchedEffect(currentTheme) {
        selectedTheme = currentTheme
    }
    val themes = remember {
        ThemePreference.entries.map { pref ->
            val displayName = when (pref) {
                ThemePreference.MATERIAL_YOU -> "Material You (Dynamic)"
                ThemePreference.LIGHT -> "Light"
                ThemePreference.DARK -> "Dark"
                ThemePreference.SYSTEM -> "System"
            }
            Pair(pref, displayName)
        }
    }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {goBack()},
        confirmButton = {
            Button(onClick = {
                coroutineScope.launch {
                    userPreference.setTheme(selectedTheme)
                    goBack()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { goBack() }) {
                Text("Cancel")
            }
                        },
        title = { Text("Theme") },
        text = {
            Column(Modifier.selectableGroup()){
                themes.forEach { (themeEnum, themeName) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (themeEnum == selectedTheme),
                                onClick = { selectedTheme = themeEnum },
                                role = Role.RadioButton
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (themeEnum == selectedTheme),
                            onClick = null
                        )
                        Text(
                            text = themeName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    )

}

@Preview
@Composable
fun PreviewThemeSelector() {
    val context = LocalContext.current
    LeoFindItTheme {
        ThemeSelector(UserPreferencesRepository(dataStore = context.appSettingsDataStore), currentTheme = ThemePreference.LIGHT)
    }
}