package com.mutissx.dicechallenge

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mutissx.dicechallenge.presentation.components.BottomBar
import com.mutissx.dicechallenge.presentation.navigation.AppNavHost
import com.mutissx.dicechallenge.presentation.navigation.bottomBarRoutes
import com.mutissx.dicechallenge.ui.theme.DICEChallengeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(TRANSPARENT)
        )
        setContent {
            DICEChallengeTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in bottomBarRoutes) {
                            BottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(navController = navController, innerPadding = innerPadding)
                }
            }
        }
    }
}