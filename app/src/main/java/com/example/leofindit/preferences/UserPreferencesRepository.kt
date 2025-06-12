package com.example.leofindit.preferences

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class UserPreferencesRepository (private val dataStore: DataStore<Preferences>) {
    val isFirstLaunch : Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[UserPreferences.IS_FIRST_LAUNCH] ?: true
    }
    suspend fun setFirstLaunch(value: Boolean) {
        dataStore.edit { preferences->
            preferences[UserPreferences.IS_FIRST_LAUNCH] = value
        }
    }
    val getCurrentTheme : Flow<ThemePreference> = dataStore.data.map { selected ->
        val themeString = selected[UserPreferences.SELECTED_THEME]
        return@map ThemePreference.entries.find { it.name == themeString } ?: ThemePreference.DARK
    }
    suspend fun setTheme(theme : ThemePreference) {
        dataStore.edit { preference ->
            preference[UserPreferences.SELECTED_THEME] = theme.name
        }
    }
}