package victor.android.textify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import victor.android.textify.ui.notes.NotesScreen
import victor.android.textify.ui.record.RecordRoute
import victor.android.textify.ui.settings.SettingsScreen

/** Destinos de navegación (rutas con tipo). */
@Serializable
data object HomeRoute

@Serializable
data object NotesRoute

@Serializable
data object SettingsRoute

@Composable
fun TextifyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            RecordRoute()
        }
        composable<NotesRoute> {
            NotesScreen(onNavigateToRecord = { navController.navigateToTab(HomeRoute) })
        }
        composable<SettingsRoute> {
            SettingsScreen()
        }
    }
}

inline fun <reified T : Any> NavHostController.navigateToTab(route: T) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
