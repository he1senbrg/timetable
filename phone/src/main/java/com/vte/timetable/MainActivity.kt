package com.vte.timetable

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.vte.timetable.MainActivity.Companion.DATA_PATH
import com.vte.timetable.MainActivity.Companion.TAG
import com.vte.timetable.ui.theme.TimeTableTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity(){

    private val messageState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            TimeTableTheme {
                val navController = rememberNavController()
                Surface(color = MaterialTheme.colorScheme.surface) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home",
                            enterTransition = { return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                            exitTransition = { return@composable slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                            popEnterTransition = { return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                            popExitTransition = { return@composable slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
                        ) {
                            HomeScreen(navController,this@MainActivity)
                        }
                        composable("create?textNow={textNow}",
                            arguments = listOf(
                                navArgument("textNow") { type = NavType.StringType ; defaultValue = "" },
                            ),
                            enterTransition = { return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                            exitTransition = { return@composable slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                            popEnterTransition = { return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                            popExitTransition = { return@composable slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
                        ) {
                            CreateScreen(it.arguments?.getString("textNow") ?: "",navController,this@MainActivity)
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
    val message = readConfigFromFile(context,"time-table.json").toString()
    val putDataMapRequest = PutDataMapRequest.create("/data_path").apply {
        dataMap.putString("message", message)
    }
    val putDataRequest = putDataMapRequest.asPutDataRequest()
    Wearable.getDataClient(context).putDataItem(putDataRequest)
        .addOnSuccessListener {
            Log.d("SendToWear", "Data item sent successfully: $message")
        }
        .addOnFailureListener {
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





/*
override fun onDataChanged(dataEvents: DataEventBuffer) {
    for (event in dataEvents) {
        if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == DATA_PATH) {
            val dataMap = event.dataItem.data?.let { DataMap.fromByteArray(it) }
            val message = dataMap?.getString("message")
            Log.d(TAG, "Data item received: $message")
            lifecycleScope.launch(Dispatchers.Main) {
                message?.let {
                    messageState.value = it
                }
            }
        } else {
            Log.d(TAG, "Received data with unknown path: ${event.dataItem.uri.path}")
        }
    }
}
 */