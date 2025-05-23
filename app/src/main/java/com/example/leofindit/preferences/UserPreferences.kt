package com.example.leofindit.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

data object UserPreferences {
    val IS_FIRST_LAUNCH: Preferences.Key<Boolean> = booleanPreferencesKey("is_first_launch")
}