package com.mutissx.dicechallenge.presentation.navigation

sealed class Destination(val route: String) {
    data object Search : Destination("search")
    data object Favorites : Destination("favorites")
    data object ArtistDetail : Destination("artist/{mbid}") {
        const val ARG_MBID = "mbid"
        fun createRoute(mbid: String): String = "artist/$mbid"
    }
}

val bottomBarRoutes: Set<String> = setOf(Destination.Search.route, Destination.Favorites.route)
