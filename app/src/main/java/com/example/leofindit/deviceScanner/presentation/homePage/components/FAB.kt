package com.example.leofindit.deviceScanner.presentation.homePage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.leofindit.ui.theme.GoldPrimary

@Composable
fun FAB(visible : Boolean, onClick : () -> Unit, modifier : Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }), // Slide in from bottom
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),   // Slide out to bottom
        modifier = modifier
            .padding(bottom = 24.dp) // Padding from the screen edge
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = GoldPrimary, // Use your theme color
            contentColor = Color.Black, // Adjust content color for contrast with GoldPrimary
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Scroll to top"
            )
        }
    }
}

@Preview
@Composable
fun FABPreview() {
    FAB(visible = true, onClick = {}, modifier = Modifier)
}