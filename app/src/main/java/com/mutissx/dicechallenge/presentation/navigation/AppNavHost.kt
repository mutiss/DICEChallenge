package com.mutissx.dicechallenge.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

                },
                viewModel = viewModel
            )
        }
        composable(Destination.Favorites.route) {

        }
    }
}
