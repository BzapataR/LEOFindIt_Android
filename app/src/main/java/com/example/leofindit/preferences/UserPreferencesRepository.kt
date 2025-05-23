package com.example.leofindit.preferences

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "App Settings")

class UserPreferencesRepository (private val context : Context) {
    val isFirstLaunch : Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[UserPreferences.IS_FIRST_LAUNCH] ?: true
    }
    suspend fun setFirstLaunch(value: Boolean) {
        context.dataStore.edit { preferences->
            preferences[UserPreferences.IS_FIRST_LAUNCH] = value
        }
    }
}