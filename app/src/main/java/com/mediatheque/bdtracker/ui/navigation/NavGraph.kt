package com.mediatheque.bdtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mediatheque.bdtracker.data.repository.BdRepository
import com.mediatheque.bdtracker.di.ViewModelFactory
import com.mediatheque.bdtracker.ui.detail.SeriesDetailScreen
import com.mediatheque.bdtracker.ui.detail.SeriesDetailViewModel
import com.mediatheque.bdtracker.ui.library.LibraryScreen
import com.mediatheque.bdtracker.ui.library.LibraryViewModel
import com.mediatheque.bdtracker.ui.search.SearchScreen
import com.mediatheque.bdtracker.ui.search.SearchViewModel

// Routes de navigation, centralisées pour éviter les fautes de frappe
private object Routes {
    const val BIBLIOTHEQUE = "bibliotheque"
    const val RECHERCHE = "recherche"
    const val DETAIL = "detail/{serieId}"
    fun detail(serieId: Long) = "detail/$serieId"
}

@Composable
fun BdTrackerNavGraph(repository: BdRepository) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BarreDeNavigation(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.BIBLIOTHEQUE,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Routes.BIBLIOTHEQUE) {
                val viewModel: LibraryViewModel = viewModel(factory = ViewModelFactory(repository))
                LibraryScreen(
                    viewModel = viewModel,
                    onSerieCliquee = { serieId -> navController.navigate(Routes.detail(serieId)) }
                )
            }

            composable(Routes.RECHERCHE) {
                val viewModel: SearchViewModel = viewModel(factory = ViewModelFactory(repository))
                SearchScreen(viewModel = viewModel)
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("serieId") { type = NavType.LongType })
            ) { backStackEntry ->
                val serieId = backStackEntry.arguments?.getLong("serieId") ?: 0L
                val viewModel: SeriesDetailViewModel = viewModel(
                    factory = ViewModelFactory(repository, serieId)
                )
                SeriesDetailScreen(viewModel = viewModel, onRetour = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BarreDeNavigation(navController: androidx.navigation.NavHostController) {
    val items = listOf(
        Triple(Routes.BIBLIOTHEQUE, "Ma bibliothèque", Icons.Default.Book),
        Triple(Routes.RECHERCHE, "Recherche", Icons.Default.Search)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { (route, label, icon) ->
            val selectionne = currentDestination?.hierarchy?.any { it.route == route } == true
            NavigationBarItem(
                selected = selectionne,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
