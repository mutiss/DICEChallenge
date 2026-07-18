package com.mutissx.dicechallenge.presentation.navigation

sealed class Destination(val route: String) {
    data object Search : Destination("search")
    data object Favorites : Destination("favorites")
}

val bottomBarRoutes: Set<String> = setOf(Destination.Search.route, Destination.Favorites.route)
