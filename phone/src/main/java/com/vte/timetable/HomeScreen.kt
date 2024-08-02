package com.vte.timetable

import android.content.Context
import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Calendar
import java.util.Date

@Composable
fun HomeScreen(navController: NavController,context: Context) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        floatingActionButton = { HomeFAB(navController) },
        topBar = { TopBar() },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        HomeContent(innerPadding,navController,context,snackbarHostState,scope)
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(padding: PaddingValues,navController: NavController,context: Context,snackbarHostState: SnackbarHostState,scope: CoroutineScope) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        CurrentCard(nameOfTable = "Monday",navController = navController,context = context,snackbarHostState = snackbarHostState,scope = scope)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
   TopAppBar(
       title = { Text(text = "Time Table") },
       navigationIcon = {}
       )
}


@Composable
fun HomeFAB(navController: NavController) {
    FloatingActionButton(onClick = { navController.navigate("create?textNow=Create") }) {
        Icon(Icons.Rounded.Edit, contentDescription = "Edit")
    }
}

@Composable
fun CurrentCard(nameOfTable:String,height:Int = 160,navController: NavController,context: Context,snackbarHostState: SnackbarHostState,scope: CoroutineScope) {
    val days = listOf("None","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
    var selectedDay by remember { mutableStateOf("") }
    val dummyJSON = JSONObject("{\"hours\":[],\"days\":[],\"classes\":[[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"]]}")


    val calendar = Calendar.getInstance()
    val currentDayInt = calendar.get(Calendar.DAY_OF_WEEK)
    val currentDay = dayMappingString(currentDayInt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(height.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text =currentDay, color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize
                )
                Spacer(modifier = Modifier.height(20.dp))
                OptionBox(
                    options = days,
                    boxLabel = "Clone from",
                    onValueChange = {
                        selectedDay = it
                        cloneAnotherDayToday(it,context)
                        scope.launch {
                            snackbarHostState.showSnackbar("Cloned from $it", withDismissAction = true, duration = SnackbarDuration.Short)
                        }
                    }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        saveConfigToFile(context,"time-table.json",dummyJSON)
                        sendMessageToWear(context)

                        scope.launch {
                            snackbarHostState.showSnackbar("Time table deleted", withDismissAction = true, duration = SnackbarDuration.Short)
                        }
                    }
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

fun cloneAnotherDayToday(chosenDay: String,context: Context) {
    val calendar = Calendar.getInstance()
    val currentDayInt = calendar.get(Calendar.DAY_OF_WEEK)
    val currentDay = dayMapping(currentDayInt)

    val currentJSON = readConfigFromFile(context,"time-table.json")?: JSONObject("{\"hours\":[],\"days\":[],\"classes\":[[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"]]}")
    val currentDaysList = currentJSON.getJSONArray("classes")

    Log.d("CurrentJSON Before",currentJSON.toString())

    val chosenDayInt = when(chosenDay) {
        "Monday" -> 0
        "Tuesday" -> 1
        "Wednesday" -> 2
        "Thursday" -> 3
        "Friday" -> 4
        "Saturday" -> 5
        "Sunday" -> 6
        else -> 7
    }

    if (chosenDayInt >= 7) {
        return
    }

    val chosenDayList = currentDaysList[chosenDayInt]

    currentDaysList.put(currentDay,chosenDayList)

    currentJSON.remove("classes")
    currentJSON.put("classes",currentDaysList)

    Log.d("CurrentJSON After",currentJSON.toString())

    saveConfigToFile(context,"time-table.json",currentJSON)

    sendMessageToWear(context)


}

fun dayMappingString(day: Int): String {
    return when(day) {
        1 -> "Sunday"
        2 -> "Monday"
        3 -> "Tuesday"
        4 -> "Wednesday"
        5 -> "Thursday"
        6 -> "Friday"
        7 -> "Saturday"
        else -> "None"
    }
}


fun dayMapping(day: Int): Int {
    return when(day) {
        1 -> 6
        2 -> 0
        3 -> 1
        4 -> 2
        5 -> 3
        6 -> 4
        7 -> 5
        else -> 7
    }
}