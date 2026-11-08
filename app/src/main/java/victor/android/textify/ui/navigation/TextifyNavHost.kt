package victor.android.textify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import victor.android.textify.ui.home.HomeScreen

/** Destinos de navegación (rutas con tipo). Se agregan más en etapas siguientes. */
@Serializable
data object HomeRoute

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
            HomeScreen()
        }
    }
}
