package victor.android.textify.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import victor.android.textify.R
import victor.android.textify.ui.navigation.HomeRoute
import victor.android.textify.ui.navigation.NotesRoute
import victor.android.textify.ui.navigation.TextifyNavHost
import victor.android.textify.ui.navigation.navigateToTab

@Composable
fun TextifyApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { TextifyBottomBar(navController) },
    ) { innerPadding ->
        TextifyNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun TextifyBottomBar(navController: NavHostController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination?.hasRoute<HomeRoute>() == true,
            onClick = { navController.navigateToTab(HomeRoute) },
            icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
            label = { Text(stringResource(R.string.record_tab_label)) },
        )
        NavigationBarItem(
            selected = currentDestination?.hasRoute<NotesRoute>() == true,
            onClick = { navController.navigateToTab(NotesRoute) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.notes_tab_label)) },
        )
    }
}
