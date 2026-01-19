package com.vte.timetable

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.util.Calendar


fun cloneAnotherDayToday(chosenDay: String, context: Context) {
    val calendar = Calendar.getInstance()
    val currentDayInt = calendar.get(Calendar.DAY_OF_WEEK)
    val currentDay = dayMapping(currentDayInt)

    val currentJSON = readConfigFromFile(context, "time-table.json")
        ?: JSONObject("{\"hours\":[],\"days\":[],\"classes\":[[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"]]}")
    val currentDaysList = currentJSON.getJSONArray("classes")

    Log.d("CurrentJSON Before", currentJSON.toString())

    val chosenDayInt = when (chosenDay) {
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

    currentDaysList.put(currentDay, chosenDayList)

    currentJSON.remove("classes")
    currentJSON.put("classes", currentDaysList)

    Log.d("CurrentJSON After", currentJSON.toString())

    saveConfigToFile(context, "time-table.json", currentJSON)

    sendMessageToWear(context)


}

fun dayMappingString(day: Int): String {
    return when (day) {
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
    return when (day) {
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