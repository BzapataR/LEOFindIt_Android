package com.example.leofindit.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
    MATERIAL_YOU
}
data object UserPreferences {
    val IS_FIRST_LAUNCH: Preferences.Key<Boolean> = booleanPreferencesKey("is_first_launch")
    val SELECTED_THEME : Preferences.Key<String> = stringPreferencesKey("selected_theme")
}