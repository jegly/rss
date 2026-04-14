package com.jegly.rss.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.jegly.rss.presentation.home.HomeScreen
import com.jegly.rss.presentation.feed_detail.FeedDetailScreen
import com.jegly.rss.presentation.article.ArticleReaderScreen
import com.jegly.rss.presentation.settings.SettingsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("feed_detail/{url}", arguments = listOf(navArgument("url") { type = NavType.StringType })) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            FeedDetailScreen(navController, url)
        }
        composable("article/{url}", arguments = listOf(navArgument("url") { type = NavType.StringType })) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            ArticleReaderScreen(navController, url)
        }
    }
}
