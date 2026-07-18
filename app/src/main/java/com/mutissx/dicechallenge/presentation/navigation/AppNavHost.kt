package com.mutissx.dicechallenge.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mutissx.dicechallenge.presentation.detail.screen.ArtistDetailScreen
import com.mutissx.dicechallenge.presentation.detail.viewmodel.ArtistDetailViewModel
import com.mutissx.dicechallenge.presentation.favorites.screen.FavoritesScreen
import com.mutissx.dicechallenge.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.dicechallenge.presentation.search.screen.SearchScreen
import com.mutissx.dicechallenge.presentation.search.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Search.route
    ) {
        composable(Destination.Search.route) {
            val viewModel: SearchViewModel = koinViewModel()
            SearchScreen(
                innerPadding = innerPadding,
                onArtistClick = { mbid ->
                    navController.navigate(Destination.ArtistDetail.createRoute(mbid))
                },
                viewModel = viewModel
            )
        }
        composable(Destination.Favorites.route) {
            val viewModel: FavoritesViewModel = koinViewModel()
            FavoritesScreen(
                innerPadding = innerPadding,
                onArtistClick = { mbid ->
                    navController.navigate(Destination.ArtistDetail.createRoute(mbid))
                },
                viewModel = viewModel
            )
        }
        composable(
            route = Destination.ArtistDetail.route,
            arguments = listOf(navArgument(Destination.ArtistDetail.ARG_MBID) {
                type = NavType.StringType
            })
        ) {
            val viewModel: ArtistDetailViewModel = koinViewModel()
            ArtistDetailScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
