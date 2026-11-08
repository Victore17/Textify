package victor.android.textify.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import victor.android.textify.ui.navigation.TextifyNavHost

@Composable
fun TextifyApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        TextifyNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
