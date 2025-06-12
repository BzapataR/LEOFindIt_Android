package com.example.leofindit.deviceScanner.presentation.universalComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.leofindit.R
import com.example.leofindit.ui.theme.LeoFindItTheme

//********************************************************************************
//                    not used navigation bar to navigate to
//                    home and settings page
//********************************************************************************
@Composable
fun BottomBar(navController: NavController? = null) {
    val items = listOf( "Scan", "Settings")
    val navList = listOf("Manual Scan", "Settings")
    var selectedItem by remember { mutableIntStateOf(0) }
    val selectedIcon = listOf(
        ImageVector.vectorResource(R.drawable.baseline_search_24),
        ImageVector.vectorResource(R.drawable.baseline_settings_24)
    )
    val unselectedIcon = listOf(
        ImageVector.vectorResource(R.drawable.outline_search_24),
        ImageVector.vectorResource(R.drawable.outline_settings_24)
    )

    val navBackStackEntry = navController?.currentBackStackEntryAsState()?.value
    val currentDestination = navBackStackEntry?.destination?.route
    NavigationBar (
        modifier = Modifier
            .fillMaxWidth(),
           // .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.onPrimary

        ) {
        items.forEachIndexed { index, item->
        NavigationBarItem(
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.surface,
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            icon = {
                Icon(
                    if (selectedItem == index) selectedIcon[index] else unselectedIcon[index],
                    contentDescription = item
                )
            },
            label = { Text( text = items[index] ) },
            onClick = {
                    selectedItem = index
                    if(currentDestination != navList[index])
                        navController?.navigate(navList[index])
            },
            selected = selectedItem == index,
            alwaysShowLabel = false
        )
        }
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    LeoFindItTheme {
        Surface {
            BottomBar()
        }
    }
}