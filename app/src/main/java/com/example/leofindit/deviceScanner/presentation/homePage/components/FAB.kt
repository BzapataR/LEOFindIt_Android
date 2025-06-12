package com.example.leofindit.deviceScanner.presentation.homePage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FAB(modifier : Modifier, lazyListState: LazyListState) {
    var fabVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    // Condition for showing FAB: true if scrolled past the first item.
    val pastFirstIndex by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }


    LaunchedEffect(pastFirstIndex, lazyListState.isScrollInProgress) {
        if (pastFirstIndex) {
            fabVisible = true
            autoHideJob?.cancel() // Cancel any existing auto-hide job

            if (!lazyListState.isScrollInProgress) {
                // If scrolling has stopped, start the auto-hide timer
                autoHideJob = coroutineScope.launch {
                    delay(2000L)
                    fabVisible = false // Then hide the FAB
                }
            }
        } else {
            // If we are at the top of the list or initially
            fabVisible = false
            autoHideJob?.cancel()
        }
    }
    AnimatedVisibility(
        visible = fabVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }), // Slide in from bottom
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),   // Slide out to bottom
        modifier = modifier
            .padding(bottom = 24.dp) // Padding from the screen edge
    ) {
        FabIcon(onClick = {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        })
    }
}
@Composable
fun FabIcon(onClick : () -> Unit) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .75f), // Use your theme color
            contentColor = Color.Black, // Adjust content color for contrast with MaterialTheme.colorScheme.primary
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Scroll to top"
            )
        }
}

@Preview
@Composable
fun FABPreview() {
    FabIcon(onClick = {})
}