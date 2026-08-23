package com.jegly.rss.presentation.navigation
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.jegly.rss.presentation.article.ArticleReaderScreen
import com.jegly.rss.presentation.feed_detail.FeedDetailScreen
import com.jegly.rss.presentation.home.HomeScreen
import com.jegly.rss.presentation.saved.SavedArticlesScreen
import com.jegly.rss.presentation.settings.SettingsScreen
import com.jegly.rss.presentation.settings.SettingsViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    // Created here (Activity scope, outside NavHost) so it matches the instance
    // used by SecureRSSTheme — theme changes are visible immediately.
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, settingsViewModel = settingsViewModel) }
        composable("settings") { SettingsScreen(navController, viewModel = settingsViewModel) }
        composable("saved") { SavedArticlesScreen(navController) }
        composable(
            "feed_detail/{url}?title={title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            FeedDetailScreen(navController, url, feedTitle = title)
        }
        composable(
            "article/{url}?title={title}&feedTitle={feedTitle}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
                navArgument("feedTitle") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val feedTitle = backStackEntry.arguments?.getString("feedTitle") ?: ""
            ArticleReaderScreen(navController, url, articleTitle = title, feedTitle = feedTitle, settingsViewModel = settingsViewModel)
        }
    }
}
