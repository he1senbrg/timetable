package com.vte.timetable.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.vte.timetable.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {


    companion object {
        private const val TAG = "MainWearActivity"
        private const val MESSAGE_PATH = "/data_path"
    }

    private fun getNodes(): List<String> {
        return Tasks.await(Wearable.getNodeClient(this).connectedNodes).map { it.id }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        CoroutineScope(Dispatchers.IO).launch {
            val nodes = getNodes()
            println("Found nodes: $nodes")
        }
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable(route = "home",
                    enterTransition = { return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(500)) },
                    exitTransition = { return@composable slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(500)) },
                    popEnterTransition = { return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(500)) },
                    popExitTransition = { return@composable slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(500)) }
                ) {
                    HomeScreen(navController)
                }
                composable(route = "days?day={day}&course={course}",
                    arguments = listOf(
                        navArgument("day") { type = androidx.navigation.NavType.StringType },
                        navArgument("course") { type = androidx.navigation.NavType.StringType },
                    ),
                    enterTransition = { return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(500)) },
                    exitTransition = { return@composable slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(500)) },
                    popEnterTransition = { return@composable slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(500)) },
                    popExitTransition = { return@composable slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(500)) }

                ) {
                    DayScreen(navController,it.arguments?.getString("day")?:"", it.arguments?.getString("course")?:"")
                }
            }
        }
        Wearable.getDataClient(this).addListener(this)
    }

    @Composable
    fun TextForTableDay(dayText: String){
        Text(text = dayText, fontSize = 20.sp)
    }

    @Composable
    fun TextForTablePeriod(tableText: String, subText: String){
        Text(text = tableText, fontSize = 20.sp)
        Text(text = subText, fontSize = 16.sp)
    }

    @Composable
    fun ListViewT(navController: NavController, scalingLazyListState: ScalingLazyListState) {
        val focusRequester = remember { FocusRequester() }
        val coroutineScope = rememberCoroutineScope()
        val timeTableJson = readConfigFromFile(this, "time-table.json")
        val listOfDays = timeTableJson?.getJSONArray("days")
        ScalingLazyColumn(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxSize()
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        scalingLazyListState.scrollBy(it.verticalScrollPixels)
                        if (it.verticalScrollPixels > 0) {
                            scalingLazyListState.animateScrollToItem((scalingLazyListState.centerItemIndex) + (1 / 2))
                        } else if (it.verticalScrollPixels < 0) {
                            scalingLazyListState.animateScrollToItem((scalingLazyListState.centerItemIndex) - (1 / 2))
                        }
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            state = scalingLazyListState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = scalingLazyListState, snapOffset = 0.dp)
        ) {
            item {
                TextForTableDay("Time Table")
            }
            Log.d(TAG, "List of days: $listOfDays")
            if (listOfDays != null) {
                items(listOfDays.length()) {
                    val currentCourses = mapDaysToCourse(listOfDays.getString(it),this@MainActivity).toString()
                    Card(
                        onClick = { navController.navigate("days?day=${listOfDays.get(it)}&course=$currentCourses") },
                        modifier = Modifier
                            .height(50.dp)
                            .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                        backgroundPainter = CardDefaults.cardBackgroundPainter(startBackgroundColor = Color.Black, endBackgroundColor = Color.Black)
                    ) {
                        TextForTableDay(listOfDays.getString(it))
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        val nodeClient = Wearable.getNodeClient(this@MainActivity)
                        val messageClient = Wearable.getMessageClient(this@MainActivity)

                        nodeClient.connectedNodes.addOnSuccessListener { node ->
                            if (node.isNotEmpty()) {
                                val nodeId = node[node.lastIndex].id
                                Log.d(TAG, "Sending message to node: $nodeId")
                                val path = "/open-phone-app"
                                val message = "Open the phone app"
                                messageClient.sendMessage(nodeId, path, message.toByteArray())
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(16.dp)
                        .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.secondaryButtonColors(backgroundColor = Color.Transparent),
                ) {
                    Icon(painterResource(id = R.drawable.rounded_open_in_phone_24), contentDescription = "Open Phone App", tint = Color.White)
                }

            }
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    @Composable
    fun ListViewDay(currentDay: String,periodList: JSONArray, scalingLazyListState: ScalingLazyListState ,navController: NavController) {
        val focusRequester = remember{ FocusRequester() }
        val coroutineScope = rememberCoroutineScope()

        val timeTableJson = readConfigFromFile(this, "time-table.json")
        val listOfHours = timeTableJson?.getJSONArray("hours")?:JSONArray(listOf("9.00","9.50","10.40","11.40","12.30","1.20","2.10","3.00","3.10","4.00","4.50"))
        ScalingLazyColumn(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxSize()
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        scalingLazyListState.scrollBy(it.verticalScrollPixels)
                        if (it.verticalScrollPixels > 0) {
                            scalingLazyListState.animateScrollToItem((scalingLazyListState.centerItemIndex) + (1 / 2))
                        } else if (it.verticalScrollPixels < 0) {
                            scalingLazyListState.animateScrollToItem((scalingLazyListState.centerItemIndex) - (1 / 2))
                        }
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            state = scalingLazyListState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = scalingLazyListState, snapOffset = 0.dp)
        ) {
            item {
                TextForTableDay(currentDay)
            }
            items(periodList.length()) {
                Card(
                    onClick = {},
                    modifier = Modifier
                        .height(70.dp)
                        .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                    backgroundPainter = CardDefaults.cardBackgroundPainter(startBackgroundColor = Color.Black, endBackgroundColor = Color.Black)
                ){
                    TextForTablePeriod(periodList.get(it).toString(),"${listOfHours.get(it)} - ${listOfHours.get(it+1)}")
                }
            }
            item {
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(16.dp)
                        .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.secondaryButtonColors(backgroundColor = Color.Transparent),
                ) {
                    Icon(painterResource(id = R.drawable.round_arrow_back_ios_24), contentDescription = "Back", tint = Color.White)
                }

            }
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    @Composable
    fun DayScreen(navController: NavController,currentDay:String , pList: String){
        val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()
        val jsonFromListTimeTable = JSONArray(pList)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = { Text(text = "Time") },
            vignette = {},
            positionIndicator = {PositionIndicator(scalingLazyListState = scalingLazyListState)}
        ) {
            ListViewDay(currentDay,jsonFromListTimeTable,scalingLazyListState,navController)
        }
    }



    @Composable
    fun HomeScreen(navController: NavController){
        val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = {},
            vignette = {},
            positionIndicator = {PositionIndicator(scalingLazyListState = scalingLazyListState)
            }
        ) {
            ListViewT(navController = navController,scalingLazyListState)
        }
    }




    @Composable
    fun JSONWriteButton() {
        Card(
            onClick = {
                Log.d(TAG, "Saving...")
                val configData = JSONObject()
                configData.put("userName", "Vishnu")
                configData.put("notificationsEnabled", true)
                configData.put("theme", "dark")
                saveConfigToFile(this, "config.json", configData)
                Log.d(TAG, "JSON saved $configData")
            },
            modifier = Modifier
                .height(50.dp)
                .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(20.dp)),
            backgroundPainter = CardDefaults.cardBackgroundPainter(startBackgroundColor = Color.Black, endBackgroundColor = Color.Black)
        ) {
            TextForTableDay("Write JSON")
        }
    }

    @Composable
    fun JSONReadButton() {
        Card(
            onClick = {
                Log.d(TAG, "Reading...")
                val configFile = "config.json"
                val configReadData: JSONObject? = readConfigFromFile(this, configFile)
                Log.d(TAG, "JSON read: $configReadData")
            },
            modifier = Modifier
                .height(50.dp)
                .border(1.dp, Color.DarkGray, shape = RoundedCornerShape(20.dp)),
            backgroundPainter = CardDefaults.cardBackgroundPainter(startBackgroundColor = Color.Black, endBackgroundColor = Color.Black)
        ) {
            TextForTableDay("Read JSON")
        }
    }


    /*

    @Composable
    fun DeployButton() {
        val currentMessage = messageState.value
        Button(onClick = {
            Log.d(TAG, "Deploy button clicked, message: $currentMessage")
            lifecycleScope.launch(Dispatchers.IO) {
                sendDataToPhone()
            }
        }) {
            Text(text = "Deploy")
        }
    }

    private fun sendDataToPhone() {
        val dataMap = PutDataMapRequest.create(MESSAGE_PATH)
        dataMap.dataMap.putString("message", "cello")
        val putDataRequest = dataMap.asPutDataRequest()
        val dataItemTask = Wearable.getDataClient(this).putDataItem(putDataRequest)
        try {
            val dataItem = Tasks.await(dataItemTask)
            Log.d(TAG, "Data sent successfully: $dataItem")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send data", e)
        }
    }
    */

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                // fgb
            }

            val dataItem = event.dataItem
            if (dataItem.uri.path == MESSAGE_PATH) {
                val dataMapItem = DataMapItem.fromDataItem(dataItem)
                val message = dataMapItem.dataMap.getString("message")?:""

                val jsonFromMessage = JSONObject(message)

                Log.d(TAG,"map :${dataMapItem.dataMap}")
                Log.d(TAG, "Message: $message")
                Log.d(TAG, "JSON: $jsonFromMessage")

                Log.d(TAG, "Saving JSON to file")
                saveConfigToFile(this, "time-table.json", jsonFromMessage)
                Log.d(TAG, "JSON saved")

                Log.d(TAG, "Reading JSON from file")
                val dataFromJson = readConfigFromFile(this, "time-table.json")
                Log.d(TAG, "Data from JSON: $dataFromJson")
            }
        }
    }
}

fun mapDaysToCourse(day: String,context: Context): Any? {
    val jsonFromTimeTable = readConfigFromFile(context, "time-table.json")
    var courseList = JSONArray(listOf("1","2","3","4","5","6","7"))
    courseList = jsonFromTimeTable?.getJSONArray("classes")?:courseList
    return when(day){
        "Monday" -> courseList.get(0)
        "Tuesday" -> courseList.get(1)
        "Wednesday" -> courseList.get(2)
        "Thursday" -> courseList.get(3)
        "Friday" -> courseList.get(4)
        "Saturday" -> courseList.get(5)
        "Sunday" -> courseList.get(6)
        else -> {
            JSONArray(listOf("1","2","3","4","5","6","7"))
        }
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



val ListMonday: List<String> = listOf("Monday","EEE","MFC","UI Design","ADM","Break","EOC Lab","EOC Lab","Break","Break")
val ListTuesday: List<String> = listOf("Tuesday","Java","EOC","Java Lab","Java Lab","Break","MFC","Break","DSA Lab","DSA Lab")
val ListWednesday: List<String> = listOf("Wednesday","DSA","EOC","EEE","Java","Break","MFC Lab","MFC Lab","Break","Break")
val ListThursday: List<String> = listOf("Thursday","ADM","DSA","Java","MFC","Break","Break","UID Lab","UID Lab","Break")
val ListFriday: List<String> = listOf("Friday","UI Design","Ma-Om","DSA","Break","Break","EEE Lab","EEE Lab","Break","Break")