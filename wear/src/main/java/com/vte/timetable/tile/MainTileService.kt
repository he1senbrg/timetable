package com.vte.timetable.tile

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ModifiersBuilders.Corner
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.TypeBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.ButtonDefaults
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipDefaults
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.tiles.SuspendingTileService
import com.vte.timetable.R
import com.vte.timetable.emptyClickable
import com.vte.timetable.emptyPar
import com.vte.timetable.presentation.ListFriday
import com.vte.timetable.presentation.ListMonday
import com.vte.timetable.presentation.ListThursday
import com.vte.timetable.presentation.ListTuesday
import com.vte.timetable.presentation.ListWednesday
import com.vte.timetable.presentation.readConfigFromFile
import org.json.JSONArray
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar


private const val RESOURCES_VERSION = "0"

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
 class MainTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build()
    }



    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(tileLayout(this)).build()
            ).build()
        ).build()


        return TileBuilders.Tile.Builder().setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(300000)
            .setTileTimeline(singleTileTimeline).build()
    }
}

private fun tileLayout(context: Context): LayoutElementBuilders.LayoutElement {

    val cText = MultiSlotLayout.Builder()
        .addSlotContent(
            LayoutElementBuilders.Box.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT)
                .setWidth(DimensionBuilders.dp(170f))
                .setHeight(DimensionBuilders.dp(50f))
                .setModifiers(
                    Modifiers.Builder()
                        .setBorder(
                            ModifiersBuilders.Border.Builder()
                                .setWidth(DimensionBuilders.dp(0f))
                                .setColor(ColorBuilders.argb(0xffffffff.toInt()))
                                .build()
                        )
                        .setPadding(
                            ModifiersBuilders.Padding.Builder()
                                .setAll(DimensionBuilders.dp(5f))
                                .build()
                        )
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(ColorBuilders.argb(0xff0E1013.toInt()))
                            .setCorner(Corner.Builder().setRadius(DimensionBuilders.dp(15f)).build())
                            .build()
                    )
                    .build()
                )
                .addContent(
                    LayoutElementBuilders.Column.Builder()
                        .addContent(
                            LayoutElementBuilders.Text.Builder()
                                .setText("Now")
                                .setFontStyle(
                                    LayoutElementBuilders.FontStyle.Builder()
                                        .setItalic(true)
                                        .setSize(DimensionBuilders.sp(7f))
                                        .build()
                                )
                                .build()

                        )
                        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT)
                        .addContent(
                            LayoutElementBuilders.Text.Builder()
                                .setText("")
                                .setFontStyle(
                                    LayoutElementBuilders.FontStyle.Builder()
                                        .setItalic(true)
                                        .setSize(DimensionBuilders.sp(0f))
                                        .build()
                                )
                                .build()

                        )
                        .addContent(
                            LayoutElementBuilders.Text.Builder()
                                .setText(getCurrentPeriod(context))
                                .setFontStyle(
                                    LayoutElementBuilders.FontStyle.Builder()
                                        .setItalic(true)
                                        .setSize(DimensionBuilders.sp(25f))
                                        .build()
                                )
                                .build()

                        )
                        .build()
                )
                .build()
        )
        .build()

    val sTextOne = MultiSlotLayout.Builder()
        .addSlotContent(
            LayoutElementBuilders.Text.Builder()
                .setText(" ")
                .setFontStyle(
                    LayoutElementBuilders.FontStyle.Builder()
                        .setSize(DimensionBuilders.sp(30f))
                        .build()
                )
                .build()
        )
        .build()

    val sTextTwo = MultiSlotLayout.Builder()
        .addSlotContent(
            LayoutElementBuilders.Text.Builder()
                .setText(" ")
                .setFontStyle(
                    LayoutElementBuilders.FontStyle.Builder()
                        .setSize(DimensionBuilders.sp(8f))
                        .build()
                )
                .build()
        )
        .build()

    val sTextThree = MultiSlotLayout.Builder()
        .addSlotContent(
            LayoutElementBuilders.Text.Builder()
                .setText("")
                .setFontStyle(
                    LayoutElementBuilders.FontStyle.Builder()
                        .setSize(DimensionBuilders.sp(7f))
                        .build()
                )
                .build()
        )
        .build()

    val nText = MultiSlotLayout.Builder()
        .addSlotContent(
            LayoutElementBuilders.Box.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT)
                .setWidth(DimensionBuilders.dp(170f))
                .setHeight(DimensionBuilders.dp(50f))
                .setModifiers(
                    Modifiers.Builder()
                        .setBorder(
                            ModifiersBuilders.Border.Builder()
                                .setWidth(DimensionBuilders.dp(0f))
                                .setColor(ColorBuilders.argb(0xffffffff.toInt()))
                                .build()
                        )
                        .setPadding(
                            ModifiersBuilders.Padding.Builder()
                                .setAll(DimensionBuilders.dp(5f))
                                .build()
                        )
                        .setBackground(
                            ModifiersBuilders.Background.Builder()
                                .setColor(ColorBuilders.argb(0xff0E1013.toInt()))
                                .setCorner(Corner.Builder().setRadius(DimensionBuilders.dp(15f)).build())
                                .build()
                        )
                        .build()
                )
                .addContent(
                    LayoutElementBuilders.Column.Builder()
                        .addContent(
                            LayoutElementBuilders.Text.Builder()
                                .setText("Next")
                                .setFontStyle(
                                    LayoutElementBuilders.FontStyle.Builder()
                                        .setItalic(true)
                                        .setSize(DimensionBuilders.sp(7f))
                                        .build()
                                )
                                .build()

                        )
                        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT)
                        .addContent(
                            LayoutElementBuilders.Text.Builder()
                                .setText("")
                                .setFontStyle(
                                    LayoutElementBuilders.FontStyle.Builder()
                                        .setItalic(true)
                                        .setSize(DimensionBuilders.sp(0f))
                                        .build()
                                )
                                .build()

                        )
                        .addContent(
                            LayoutElementBuilders.Text.Builder()
                                .setText(getNextPeriod(context))
                                .setFontStyle(
                                    LayoutElementBuilders.FontStyle.Builder()
                                        .setItalic(true)
                                        .setSize(DimensionBuilders.sp(25f))
                                        .build()
                                )
                                .build()

                        )
                        .build()
                )
                .build()
        )
        .build()

    val refreshButton =  (
            PrimaryLayout.Builder(emptyPar)
            .setPrimaryChipContent(
                CompactChip.Builder(context, "Refresh",  emptyClickable, emptyPar)
                    .build()
            )
            .build()
    )




    val box: LayoutElementBuilders.Column = LayoutElementBuilders.Column.Builder().addContent(sTextOne).addContent(cText).addContent(sTextTwo).addContent(nText).addContent(sTextThree).addContent(refreshButton).build()
    return box
}


@Preview(
    device = Devices.WEAR_OS_LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TilePreview() {
    LayoutRootPreview(root = tileLayout(LocalContext.current))
}

val calender = Calendar.getInstance();

val current = LocalDateTime.of(
    calender.get(Calendar.YEAR),
    calender.get(Calendar.MONTH),
    calender.get(Calendar.DAY_OF_MONTH),
    calender.get(Calendar.HOUR_OF_DAY),
    calender.get(Calendar.MINUTE),
    calender.get(Calendar.SECOND)
)

fun getListOfDay(context: Context):JSONArray {
    val currentJSON = readConfigFromFile(context,"time-table.json")
    val classes = currentJSON?.getJSONArray("classes")?: JSONArray(listOf("",""))
    Log.d("Classes",classes.toString())
    return classes
}

fun getCurrentPeriod(context: Context): String {
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val cPeriod = getPeriodIndex(context)

    val classes = getListOfDay(context)
    Log.d("Classes",classes.toString())
    Log.d("Classes Friday",JSONArray(classes[4].toString().split(",")).toString())

    val periodMap = mapOf(
        Calendar.MONDAY to JSONArray(getReplacedArray(classes[0])),
        Calendar.TUESDAY to JSONArray(getReplacedArray(classes[1])),
        Calendar.WEDNESDAY to JSONArray(getReplacedArray(classes[2])),
        Calendar.THURSDAY to JSONArray(getReplacedArray(classes[3])),
        Calendar.FRIDAY to JSONArray(getReplacedArray(classes[4])),
        Calendar.SATURDAY to JSONArray(getReplacedArray(classes[5])),
        Calendar.SUNDAY to JSONArray(getReplacedArray(classes[6]))
    )

    if (periodMap[currentDay]?.get(0)?.toString() == "Nothing!") {
        return "Nothing!"
    } else {
        if (cPeriod >= periodMap[currentDay]?.length()!!) {
            return "Nothing!"
        } else {
            periodMap[currentDay]?.get(cPeriod)?.let { Log.d("Current Period", it.toString()) }
            return periodMap[currentDay]?.get(cPeriod)?.toString() ?: "Nothing!"
        }
    }
}

fun getReplacedArray(dayArray:Any) : List<String> {
    val dayArrayClean = dayArray.toString().replace("[","").replace("]","").replace("\"","").split(",")
    return if (dayArrayClean.toString() == "[]") {
        listOf("Nothing!")
    } else {
        dayArrayClean
    }
}

fun getNextPeriod(context: Context): String {
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val cPeriod = getPeriodIndex(context)

    val classes = getListOfDay(context)

    val periodMap = mapOf(
        Calendar.MONDAY to JSONArray(getReplacedArray(classes[0])),
        Calendar.TUESDAY to JSONArray(getReplacedArray(classes[1])),
        Calendar.WEDNESDAY to JSONArray(getReplacedArray(classes[2])),
        Calendar.THURSDAY to JSONArray(getReplacedArray(classes[3])),
        Calendar.FRIDAY to JSONArray(getReplacedArray(classes[4])),
        Calendar.SATURDAY to JSONArray(getReplacedArray(classes[5])),
        Calendar.SUNDAY to JSONArray(getReplacedArray(classes[6]))
    )

    val nextPeriod = when (cPeriod) {
        in 0..classes.length() -> cPeriod + 1
        else -> 0
    }

    if (periodMap[currentDay]?.get(0)?.toString() == "Nothing!") {
        return "Nothing!"
    } else {
        if (nextPeriod >= periodMap[currentDay]?.length()!!) {
            return "Nothing!"
        } else {
            periodMap[currentDay]?.get(nextPeriod)?.let { Log.d("Next Period", it.toString()) }
            return periodMap[currentDay]?.get(nextPeriod)?.toString() ?: "Nothing!"
        }
    }

}

fun isCurrentTimeInRange(start: LocalTime, end: LocalTime): Boolean {
    val currentTime = LocalTime.now()
    return !currentTime.isBefore(start) && !currentTime.isAfter(end)
}

fun getSlotFromJSON(context: Context):JSONArray {
    val currentJSON = readConfigFromFile(context,"time-table.json")
    val slots = currentJSON?.getJSONArray("hours")?: JSONArray(listOf("",""))
    return slots
}

fun getPeriodIndex(context: Context): Int {
    val slots = getSlotFromJSON(context)
    Log.d("slots Before",slots.toString())
    for (i in 0 until slots.length()) {
        var slot = slots[i]
        slot = slot.toString()
        slots.put(i,slot)
    }
    Log.d("slots After",slots.toString())


    val periods = mutableListOf<Pair<Pair<LocalTime,LocalTime>,Int>>()

    for (i in 0 until slots.length()-1) {
        // Assuming slot contains "start" and "end" times as strings in "HH:mm" format
        val startTimeStr = slots.get(i) // e.g., "09:00"
        val endTimeStr = slots.get(i+1)     // e.g., "09:50"

        var firstInt = startTimeStr.toString().split(":").first().toInt()
        var secondInt = startTimeStr.toString().split(":").last().toInt()

        // Parse strings to LocalTime
        val startTime = LocalTime.of(firstInt,secondInt)

        firstInt = endTimeStr.toString().split(":").first().toInt()
        secondInt = endTimeStr.toString().split(":").last().toInt()

        val endTime = LocalTime.of(firstInt,secondInt)

        // Add the period to the list
        periods.add(Pair(Pair(startTime, endTime), i))
    }

    Log.d("Final classes",periods.toString())

    for ((range, index) in periods) {

        if (isCurrentTimeInRange(range.first, range.second)) {
            return index
        }
    }

    return 0
}