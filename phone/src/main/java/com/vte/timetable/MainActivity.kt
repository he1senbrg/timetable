package com.vte.timetable

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.vte.timetable.ui.theme.TimeTableTheme
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {

    private val messageState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            TimeTableTheme {
                val navController = rememberNavController()
                var selectedTab by remember { mutableIntStateOf(0) }

                Surface(color = MaterialTheme.colorScheme.surface) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = {
                                    Icon(
                                        Icons.Rounded.DateRange,
                                        contentDescription = "Time Table"
                                    )
                                },
                                    label = { Text("Time Table") },
                                    selected = selectedTab == 0,
                                    onClick = {
                                        selectedTab = 0
                                        navController.navigate("canvas") {
                                            popUpTo("canvas") { inclusive = true }
                                        }
                                    })
                                NavigationBarItem(
                                    icon = {
                                    Icon(
                                        Icons.Rounded.Settings, contentDescription = "Manage"
                                    )
                                },
                                    label = { Text("Manage") },
                                    selected = selectedTab == 1,
                                    onClick = {
                                        selectedTab = 1
                                        navController.navigate("manage") {
                                            popUpTo("canvas")
                                        }
                                    })
                            }
                        }) { padding ->
                        NavHost(navController = navController, startDestination = "canvas") {
                            composable("canvas", enterTransition = {
                                return@composable slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
                                )
                            }, exitTransition = {
                                return@composable slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End, tween(300)
                                )
                            }, popEnterTransition = {
                                return@composable slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
                                )
                            }, popExitTransition = {
                                return@composable slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End, tween(300)
                                )
                            }) {
                                CanvasScreen(this@MainActivity, padding)
                            }
                            composable("manage", enterTransition = {
                                return@composable slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
                                )
                            }, exitTransition = {
                                return@composable slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End, tween(300)
                                )
                            }, popEnterTransition = {
                                return@composable slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
                                )
                            }, popExitTransition = {
                                return@composable slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End, tween(300)
                                )
                            }) {
                                ManageScreen(navController, this@MainActivity, padding)
                            }
                            composable(
                                "create?textNow={textNow}", arguments = listOf(
                                navArgument("textNow") {
                                    type = NavType.StringType; defaultValue = ""
                                },
                            ), enterTransition = {
                                return@composable slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    tween(300)
                                )
                            }, exitTransition = {
                                return@composable slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    tween(300)
                                )
                            }, popEnterTransition = {
                                return@composable slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    tween(300)
                                )
                            }, popExitTransition = {
                                return@composable slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    tween(300)
                                )
                            }) {
                                CreateScreen(
                                    it.arguments?.getString("textNow") ?: "",
                                    navController,
                                    this@MainActivity,
                                    padding
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "PhoneMainActivity"
        private const val DATA_PATH = "/data_path"
    }


}

fun sendMessageToWear(context: Context) {
    val message = readConfigFromFile(context, "time-table.json").toString()
    val putDataMapRequest = PutDataMapRequest.create("/data_path").apply {
        dataMap.putString("message", message)
    }
    val putDataRequest = putDataMapRequest.asPutDataRequest()
    Wearable.getDataClient(context).putDataItem(putDataRequest).addOnSuccessListener {
        Log.d("SendToWear", "Data item sent successfully: $message")
    }.addOnFailureListener {
        Log.d("SendToWear", "Data item failed to send: $message")
    }
}

fun saveConfigToFile(context: Context, fileName: String, jsonData: JSONObject) {
    val file = File(context.filesDir, fileName)
    try {
        FileOutputStream(file).use { outputStream ->
            outputStream.write(jsonData.toString().toByteArray())
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun readConfigFromFile(context: Context, fileName: String): JSONObject? {
    val file = File(context.filesDir, fileName)
    return if (file.exists()) {
        try {
            FileInputStream(file).use { inputStream ->
                val bytes = ByteArray(file.length().toInt())
                inputStream.read(bytes)
                JSONObject(String(bytes))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}
