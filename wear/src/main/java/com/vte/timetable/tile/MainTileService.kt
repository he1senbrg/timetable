package com.vte.timetable.tile

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ModifiersBuilders.Corner
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.tiles.SuspendingTileService
import com.vte.timetable.emptyClickable
import com.vte.timetable.emptyPar
import com.vte.timetable.presentation.readConfigFromFile
import org.json.JSONArray
import java.time.LocalTime
import java.util.Calendar


private const val RESOURCES_VERSION = "0"

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
            .setFreshnessIntervalMillis(300000).setTileTimeline(singleTileTimeline).build()
    }
}

private fun tileLayout(context: Context): LayoutElementBuilders.LayoutElement {

    val cText = MultiSlotLayout.Builder().addSlotContent(
        LayoutElementBuilders.Box.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT)
            .setWidth(DimensionBuilders.dp(170f)).setHeight(DimensionBuilders.dp(55f)).setModifiers(
                Modifiers.Builder().setBorder(
                    ModifiersBuilders.Border.Builder().setWidth(DimensionBuilders.dp(0f))
                        .setColor(ColorBuilders.argb(0xff383838.toInt())).build()
                ).setPadding(
                    ModifiersBuilders.Padding.Builder().setAll(DimensionBuilders.dp(5f))
                        .setStart(DimensionBuilders.dp(10f)).build()
                ).setBackground(
                    ModifiersBuilders.Background.Builder()
                        .setColor(ColorBuilders.argb(0xff000000.toInt())).setCorner(
                            Corner.Builder().setRadius(DimensionBuilders.dp(15f)).build()
                        ).build()
                ).build()
            ).addContent(
                LayoutElementBuilders.Column.Builder().addContent(
                    LayoutElementBuilders.Text.Builder().setText("Now").setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder().setColor(
                            ColorBuilders.argb(0xffc5c5c5.toInt())
                        ).setSize(DimensionBuilders.sp(10f)).build()
                    ).build()

                ).setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT).addContent(
                    LayoutElementBuilders.Text.Builder().setText("").setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder().setItalic(true)
                            .setSize(DimensionBuilders.sp(0f)).build()
                    ).build()

                ).addContent(
                    LayoutElementBuilders.Text.Builder().setText(getCurrentPeriod(context))
                        .setFontStyle(
                            LayoutElementBuilders.FontStyle.Builder()
                                .setSize(DimensionBuilders.sp(25f)).build()
                        ).build()

                ).build()
            ).build()
    ).build()

    val sTextOne = MultiSlotLayout.Builder().addSlotContent(
        LayoutElementBuilders.Text.Builder().setText(" ").setFontStyle(
            LayoutElementBuilders.FontStyle.Builder().setSize(DimensionBuilders.sp(30f)).build()
        ).build()
    ).build()

    val sTextTwo = MultiSlotLayout.Builder().addSlotContent(
        LayoutElementBuilders.Text.Builder().setText(" ").setFontStyle(
            LayoutElementBuilders.FontStyle.Builder().setSize(DimensionBuilders.sp(8f)).build()
        ).build()
    ).build()

    val sTextThree = MultiSlotLayout.Builder().addSlotContent(
        LayoutElementBuilders.Text.Builder().setText("").setFontStyle(
            LayoutElementBuilders.FontStyle.Builder().setSize(DimensionBuilders.sp(7f)).build()
        ).build()
    ).build()

    val nText = MultiSlotLayout.Builder().addSlotContent(
        LayoutElementBuilders.Box.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT)
            .setWidth(DimensionBuilders.dp(170f)).setHeight(DimensionBuilders.dp(55f)).setModifiers(
                Modifiers.Builder().setBorder(
                    ModifiersBuilders.Border.Builder().setWidth(DimensionBuilders.dp(0f))
                        .setColor(ColorBuilders.argb(0xff383838.toInt())).build()
                ).setPadding(
                    ModifiersBuilders.Padding.Builder().setAll(DimensionBuilders.dp(5f))
                        .setStart(DimensionBuilders.dp(10f)).build()
                ).setBackground(
                    ModifiersBuilders.Background.Builder()
                        .setColor(ColorBuilders.argb(0xff000000.toInt())).setCorner(
                            Corner.Builder().setRadius(DimensionBuilders.dp(15f)).build()
                        ).build()
                ).build()
            ).addContent(
                LayoutElementBuilders.Column.Builder().addContent(
                    LayoutElementBuilders.Text.Builder().setText("Next").setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder().setColor(
                            ColorBuilders.argb(0xffc5c5c5.toInt())
                        ).setSize(DimensionBuilders.sp(10f)).build()
                    ).build()

                ).setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_LEFT).addContent(
                    LayoutElementBuilders.Text.Builder().setText("").setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder().setItalic(true)
                            .setSize(DimensionBuilders.sp(0f)).build()
                    ).build()

                ).addContent(
                    LayoutElementBuilders.Text.Builder().setText(getNextPeriod(context))
                        .setFontStyle(
                            LayoutElementBuilders.FontStyle.Builder()
                                .setSize(DimensionBuilders.sp(25f)).build()
                        ).build()

                ).build()
            ).build()
    ).build()

    val refreshButton = (PrimaryLayout.Builder(emptyPar).setPrimaryChipContent(
        CompactChip.Builder(context, "Refresh", emptyClickable, emptyPar).setChipColors(
            ChipColors.secondaryChipColors(
                Colors(
                    0xff2f2f2f.toInt(), 0xffffffff.toInt(), 0xff2f2f2f.toInt(), 0xffffffff.toInt()
                )
            )
        ).build()
    ).build())


    val box: LayoutElementBuilders.Column =
        LayoutElementBuilders.Column.Builder().addContent(sTextOne).addContent(cText)
            .addContent(sTextTwo).addContent(nText).addContent(sTextThree).addContent(refreshButton)
            .build()
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

fun getListOfDay(context: Context): JSONArray {
    val currentJSON = readConfigFromFile(context, "time-table.json")
    val classes = currentJSON?.getJSONArray("classes") ?: JSONArray(listOf("", ""))
    Log.d("Classes", classes.toString())
    return classes
}

fun getCurrentPeriod(context: Context): String {
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val cPeriod = getPeriodIndex(context)

    val classes = getListOfDay(context)
    Log.d("Classes", classes.toString())
    Log.d("Classes Friday", JSONArray(classes[4].toString().split(",")).toString())

    val periodMap = mutableMapOf<Int, JSONArray>()

    if (classes.length() > 0) periodMap[Calendar.MONDAY] = JSONArray(getReplacedArray(classes[0]))
    if (classes.length() > 1) periodMap[Calendar.TUESDAY] = JSONArray(getReplacedArray(classes[1]))
    if (classes.length() > 2) periodMap[Calendar.WEDNESDAY] =
        JSONArray(getReplacedArray(classes[2]))
    if (classes.length() > 3) periodMap[Calendar.THURSDAY] = JSONArray(getReplacedArray(classes[3]))
    if (classes.length() > 4) periodMap[Calendar.FRIDAY] = JSONArray(getReplacedArray(classes[4]))
    if (classes.length() > 5) periodMap[Calendar.SATURDAY] = JSONArray(getReplacedArray(classes[5]))
    if (classes.length() > 6) periodMap[Calendar.SUNDAY] = JSONArray(getReplacedArray(classes[6]))


    if (!periodMap.containsKey(currentDay)) {
        return "Nothing!"
    }

    if (periodMap[currentDay]?.get(0)?.toString() == "Nothing!") {
        return "Nothing!"
    } else if (cPeriod == -1) {
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

fun getReplacedArray(dayArray: Any): List<String> {
    val dayArrayClean =
        dayArray.toString().replace("[", "").replace("]", "").replace("\"", "").split(",")
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

    val periodMap = mutableMapOf<Int, JSONArray>()

    if (classes.length() > 0) periodMap[Calendar.MONDAY] = JSONArray(getReplacedArray(classes[0]))
    if (classes.length() > 1) periodMap[Calendar.TUESDAY] = JSONArray(getReplacedArray(classes[1]))
    if (classes.length() > 2) periodMap[Calendar.WEDNESDAY] =
        JSONArray(getReplacedArray(classes[2]))
    if (classes.length() > 3) periodMap[Calendar.THURSDAY] = JSONArray(getReplacedArray(classes[3]))
    if (classes.length() > 4) periodMap[Calendar.FRIDAY] = JSONArray(getReplacedArray(classes[4]))
    if (classes.length() > 5) periodMap[Calendar.SATURDAY] = JSONArray(getReplacedArray(classes[5]))
    if (classes.length() > 6) periodMap[Calendar.SUNDAY] = JSONArray(getReplacedArray(classes[6]))

    if (!periodMap.containsKey(currentDay)) {
        return "Nothing!"
    }

    val nextPeriod = when (cPeriod) {
        in 0..classes.length() -> cPeriod + 1
        else -> -1
    }

    if (periodMap[currentDay]?.get(0)?.toString() == "Nothing!") {
        return "Nothing!"
    } else if (nextPeriod == -1) {
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

fun getSlotFromJSON(context: Context): JSONArray {
    val currentJSON = readConfigFromFile(context, "time-table.json")
    val slots = currentJSON?.getJSONArray("hours") ?: JSONArray(listOf("", ""))
    return slots
}

fun getPeriodIndex(context: Context): Int {
    val slots = getSlotFromJSON(context)
    Log.d("slots Before", slots.toString())
    for (i in 0 until slots.length()) {
        var slot = slots[i]
        slot = slot.toString()
        slots.put(i, slot)
    }
    Log.d("slots After", slots.toString())


    val periods = mutableListOf<Pair<Pair<LocalTime, LocalTime>, Int>>()

    for (i in 0 until slots.length() - 1) {
        val startTimeStr = slots.get(i)
        val endTimeStr = slots.get(i + 1)

        var firstInt = startTimeStr.toString().split(":").first().toInt()
        var secondInt = startTimeStr.toString().split(":").last().toInt()


        val startTime = LocalTime.of(firstInt, secondInt)

        firstInt = endTimeStr.toString().split(":").first().toInt()
        secondInt = endTimeStr.toString().split(":").last().toInt()

        val endTime = LocalTime.of(firstInt, secondInt)

        periods.add(Pair(Pair(startTime, endTime), i))
    }

    Log.d("Final classes", periods.toString())

    for ((range, index) in periods) {

        Log.d("Current Time", LocalTime.now().toString())
        Log.d("Start Time", range.first.toString())
        Log.d("End Time", range.second.toString())
        if (isCurrentTimeInRange(range.first, range.second)) {
            return index
        }
    }

    return -1
}