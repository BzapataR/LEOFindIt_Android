package com.example.leofindit.Intro.presentation.location


data class LocationState (
    val isPreciseLocationGranted: Boolean = false,
    val showRational: Boolean = false,
    val showApproximateWarning: Boolean = false
)