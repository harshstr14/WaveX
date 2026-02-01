package com.example.wavex.navigation

sealed class BottomNavRoute(val route: String) {
    object Home : BottomNavRoute("home")
    object Discover : BottomNavRoute("discover")
    object Search : BottomNavRoute("search")
    object Library : BottomNavRoute("library")
}
