package com.vte.timetable

import android.content.Context
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

var timeRangeList = mutableStateListOf<String>()
var finalDayList = mutableStateListOf<String>()
var finalCourseList =  mutableStateListOf<String>()

@Composable
fun CreateScreen(textNow:String,navController: NavController,context: Context) {
    Scaffold(
        topBar = { TopBarCreate(textNow,navController = navController) },
        floatingActionButton = { SubmitButton(context,navController) }
    ) { innerPadding ->
        CreateContent(innerPadding,context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarCreate(textNow:String,navController: NavController) {
    TopAppBar(
        title = { Text(text = textNow) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack()}) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack , contentDescription = "Back")
            }
        }
    )
}


@Composable
fun CreateContent(innerPadding: PaddingValues,context: Context) {
    Box(modifier = Modifier.padding(innerPadding)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SlotsView()
            Spacer(modifier = Modifier.padding(8.dp))
            DaysView()
            Spacer(modifier = Modifier.padding(8.dp))
            CourseView()
        }
    }
}

@Composable
fun SubmitButton(context: Context,navController: NavController) {
    FloatingActionButton(
        onClick = {
            saveAsJson(context)
            Toast.makeText(context,"Time table saved!",Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    ) {
        Icon(Icons.Rounded.Check, contentDescription = "Submit")
    }
}

@Composable
fun SlotsView(){
    Column {
        Text(
            "Slots",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize
        )
        AddItemList()
    }
}

@Composable
fun CourseView(viewModel: CourseViewModel = viewModel()) {

    if (viewModel.finalCourseList.size < listDays.size) {
        repeat(listDays.size - viewModel.finalCourseList.size) {
            viewModel.finalCourseList.add("")
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // .heightIn(200.dp, 400.dp)
    ) {
        Text(
            "Course",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize
        )
        LazyColumn {
            items(listDays) { day ->
                val index = listDays.indexOf(day)
                OutlinedTextField(
                    value = viewModel.finalCourseList[index],
                    onValueChange = { viewModel.finalCourseList[index] = it;Log.d("finalCourseList", viewModel.finalCourseList.toList().toString()) },
                    label = { Text(day) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    finalCourseList = viewModel.finalCourseList
}

@Composable
fun AddItemList() {
    var items by remember { mutableStateOf(listOf<String>()) }
    var showDialExample by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("") }
    var buttonTexts = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeightIn(0.dp, 200.dp)
        ) {
            items(items) { item ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showDialExample = true
                        },
                        shape = RoundedCornerShape(30)
                    ) {
                        val index = items.indexOf(item)
                        Text(text = if (buttonTexts.size > index) buttonTexts[index] else "Select slot")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            FilledIconButton(onClick = {
                if (items.isNotEmpty()) {
                    items = items.dropLast(1)
                    buttonTexts.removeLast()
                    if (timeRangeList.isNotEmpty()){
                        timeRangeList.removeLast()
                    }
                }
            }) {
                Icon(Icons.Rounded.Delete, "delete")
            }
            Button(
                onClick = {
                    Log.d("Button", "Created a button!")
                    items = items + "Item ${items.size + 1}"
                    buttonTexts.add("Select slot")
                    Log.d("ButtonList",buttonTexts.toList().toString())
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Slot")
            }
        }
    }

    if (showDialExample) {
        RangePicker(
            onDismiss = {
                showDialExample = false
            },
            onConfirm = { time ->
                var rawTimeStart = time.split(":")[0]
                var rawTimeEnd = time.split(":")[1]
                val index = items.size - 1
                if (index >= 0 && index < buttonTexts.size) {
                    buttonTexts[index] = time
                }
                if (rawTimeStart.length == 1) {
                    rawTimeStart = "0$rawTimeStart"
                }
                if (rawTimeEnd.length == 1) {
                    rawTimeEnd = "0$rawTimeEnd"
                }
                timeRangeList.add("$rawTimeStart:$rawTimeEnd")
                showDialExample = false
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangePicker(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Log.d("TimeRangeList", timeRangeList.toString())
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    Dialog(
        onDismissRequest = {onDismiss()},
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)

    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ){
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                    Button(onClick = {
                        val selectedTime = "${timePickerState.hour}:${timePickerState.minute}"
                        // timeRangeList.add(selectedTime)
                        Log.d("smth", "Above list add")
                        Log.d("TimeRangeList", timeRangeList.toList().toString())
                        onConfirm(selectedTime)
                    }) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}


val listDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@Composable
fun DaysView() {
    val rowState = rememberLazyListState()
    Column {
        Text(
            "Days",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize
        )
        LazyRow(
            state = rowState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(2.dp)
        ) {
            items(listDays){ item ->
                val isSelected = finalDayList.contains(item)
                Spacer(modifier = Modifier.padding(2.dp))
                DayCheckBox(textDay = item,isSelected)
                Spacer(modifier = Modifier.padding(2.dp))
            }
        }
    }
}

@Composable
fun DayCheckBox(textDay:String,selectedState:Boolean) {
    InputChip(
        selected = selectedState,
        onClick = {
            if (selectedState) {
                finalDayList.remove(textDay)
            } else {
                finalDayList.add(textDay)
            }
            Log.d("finalDayList", finalDayList.toList().toString())
        },
        label = { Text(text = textDay) }
    )
}

fun saveAsJson(context: Context) {
    val updatedCourseList = mutableListOf<List<String>>()
    for (i in finalCourseList.toList()) {
        var tempList = listOf<String>()
        i.split(",").forEach {
            tempList = tempList + it
        }
        updatedCourseList.add(tempList)
    }
    val jsonObject = JSONObject()
    jsonObject.put("hours", JSONArray(timeRangeList))
    jsonObject.put("days", JSONArray(finalDayList))
    jsonObject.put("classes", JSONArray(updatedCourseList))
    val jsonString = jsonObject.toString()
    Log.d("jsonString", jsonString)
    saveConfigToFile(context = context, "time-table.json", jsonObject)

    val jsonData = readConfigFromFile(context = context, "time-table.json")
    Log.d("tableJSON", jsonData.toString())
    sendMessageToWear(context)
    finalDayList = mutableStateListOf()
    timeRangeList = mutableStateListOf()
    Log.d("CreateScreenToWear", "Data sent to wear")
}