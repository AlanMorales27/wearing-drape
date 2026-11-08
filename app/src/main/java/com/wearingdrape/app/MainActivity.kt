package com.wearingdrape.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wearingdrape.app.ui.addgarment.AddGarmentScreen
import com.wearingdrape.app.ui.closet.ClosetListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    var showSavedMessage by remember { mutableStateOf(false) }
                    NavHost(navController = navController, startDestination = "closet") {
                        composable("closet") {
                            ClosetListScreen(
                                onAddGarment = { navController.navigate("addGarment") },
                                showSavedMessage = showSavedMessage,
                                onSavedMessageShown = { showSavedMessage = false },
                            )
                        }
                        composable("addGarment") {
                            AddGarmentScreen(
                                onSaved = {
                                    showSavedMessage = true
                                    navController.popBackStack()
                                },
                                onCancel = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
