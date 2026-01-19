package com.vte.timetable

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

data class Period(
    val dayIndex: Int, val startPeriod: Int, val endPeriod: Int, val name: String
)

@Composable
fun TimetableCanvas(context: Context, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1.5f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val currentJSON = readConfigFromFile(context, "time-table.json")
        ?: JSONObject("{\"hours\":[],\"days\":[],\"classes\":[[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"]]}")

    val hours = currentJSON.optJSONArray("hours") ?: JSONArray()
    val days = currentJSON.optJSONArray("days") ?: JSONArray()
    val classes = currentJSON.optJSONArray("classes") ?: JSONArray()

    if (hours.length() == 0 || days.length() == 0 || classes.length() == 0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No timetable data available.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val periods = remember(classes.toString()) {
        parsePeriods(classes, days.length())
    }

    val calendar = Calendar.getInstance()
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentTimeInMinutes = currentHour * 60 + currentMinute

    val currentDayIndex = when (currentDayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> -1
    }

    val currentPeriodIndex = remember(currentTimeInMinutes, hours.toString()) {
        getCurrentPeriodIndex(hours, currentTimeInMinutes)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(700.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offsetX += pan.x
                    offsetY += pan.y


                    val headerHeight = 60f
                    val dayColumnWidth = 120f
                    val cellWidth = 140f
                    val cellHeight = 100f
                    val padding = 32f
                    val extraBuffer = 200f

                    val numPeriods = max(1, hours.length() - 1)
                    val contentWidth = (dayColumnWidth + numPeriods * cellWidth) * scale + padding
                    val contentHeight =
                        (headerHeight + days.length() * cellHeight) * scale + padding

                    val minOffsetX = -(contentWidth - size.width + extraBuffer).coerceAtLeast(0f)
                    val minOffsetY = -(contentHeight - size.height + extraBuffer).coerceAtLeast(0f)

                    offsetX = offsetX.coerceIn(minOffsetX, extraBuffer)
                    offsetY = offsetY.coerceIn(minOffsetY, extraBuffer)
                }
            }) {
        TimetableGrid(
            hours = hours,
            days = days,
            periods = periods,
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            currentDayIndex = currentDayIndex,
            currentPeriodIndex = currentPeriodIndex
        )
    }
}

@Composable
fun TimetableGrid(
    hours: JSONArray,
    days: JSONArray,
    periods: List<Period>,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    currentDayIndex: Int,
    currentPeriodIndex: Int
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val headerHeight = 60f
        val dayColumnWidth = 120f
        val cellWidth = 140f
        val cellHeight = 100f

        val scaledCellWidth = cellWidth * scale
        val scaledCellHeight = cellHeight * scale
        val scaledHeaderHeight = headerHeight * scale
        val scaledDayColumnWidth = dayColumnWidth * scale

        val numPeriods = max(1, hours.length() - 1)

        for (i in 0 until numPeriods) {
            val x = scaledDayColumnWidth + i * scaledCellWidth + offsetX

            if (x + scaledCellWidth > 0 && x < canvasWidth) {
                drawRoundRect(
                    color = surfaceVariantColor,
                    topLeft = Offset(x + 4f, offsetY + 4f),
                    size = Size(scaledCellWidth - 8f, scaledHeaderHeight - 8f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f * scale)
                )

                val timeText = if (i < hours.length() - 1) {
                    "${convertTo12Hour(hours.getString(i))} - ${convertTo12Hour(hours.getString(i + 1))}"
                } else {
                    convertTo12Hour(hours.getString(i))
                }

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        timeText,
                        x + scaledCellWidth / 2,
                        offsetY + scaledHeaderHeight / 2 + 6f * scale,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 14f * scale
                            isFakeBoldText = true
                        })
                }
            }
        }

        for (i in 0 until days.length()) {
            val y = scaledHeaderHeight + i * scaledCellHeight + offsetY

            if (y + scaledCellHeight > 0 && y < canvasHeight) {
                drawRoundRect(
                    color = surfaceVariantColor,
                    topLeft = Offset(offsetX + 4f, y + 4f),
                    size = Size(scaledDayColumnWidth - 8f, scaledCellHeight - 8f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f * scale)
                )

                val dayText = days.getString(i)
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        dayText,
                        offsetX + scaledDayColumnWidth / 2,
                        y + scaledCellHeight / 2 + 6f * scale,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 16f * scale
                            isFakeBoldText = true
                        })
                }
            }
        }

        for (period in periods) {
            if (period.name.isBlank()) continue

            val x = scaledDayColumnWidth + period.startPeriod * scaledCellWidth + offsetX
            val y = scaledHeaderHeight + period.dayIndex * scaledCellHeight + offsetY
            val width = (period.endPeriod - period.startPeriod + 1) * scaledCellWidth

            if (x + width > 0 && x < canvasWidth && y + scaledCellHeight > 0 && y < canvasHeight) {
                val isCurrentPeriod =
                    period.dayIndex == currentDayIndex && currentPeriodIndex >= period.startPeriod && currentPeriodIndex <= period.endPeriod

                drawRoundRect(
                    color = primaryContainerColor,
                    topLeft = Offset(x + 8f, y + 8f),
                    size = Size(width - 16f, scaledCellHeight - 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f * scale)
                )

                drawRoundRect(
                    color = if (isCurrentPeriod) primaryColor.copy(alpha = 0.5f) else outlineColor.copy(
                        alpha = 0.3f
                    ),
                    topLeft = Offset(x + 8f, y + 8f),
                    size = Size(width - 16f, scaledCellHeight - 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f * scale),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (isCurrentPeriod) 3f else 1f)
                )

                drawContext.canvas.nativeCanvas.apply {
                    val androidColor = android.graphics.Color.argb(
                        (onPrimaryContainerColor.alpha * 255).toInt(),
                        (onPrimaryContainerColor.red * 255).toInt(),
                        (onPrimaryContainerColor.green * 255).toInt(),
                        (onPrimaryContainerColor.blue * 255).toInt()
                    )

                    val paint = android.graphics.Paint().apply {
                        color = androidColor
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 18f * scale
                        isFakeBoldText = true
                        isAntiAlias = true
                    }

                    val words = period.name.split(" ", "-", "(", ")")
                    val maxWidth = width - 32f
                    val textLines = mutableListOf<String>()
                    var currentLine = ""

                    for (word in words) {
                        if (word.isEmpty()) continue
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paint.measureText(testLine) <= maxWidth) {
                            currentLine = testLine
                        } else {
                            if (currentLine.isNotEmpty()) textLines.add(currentLine)
                            currentLine = word
                        }
                    }
                    if (currentLine.isNotEmpty()) textLines.add(currentLine)

                    val lineHeight = 24f * scale
                    val totalHeight = textLines.size * lineHeight
                    val startY = y + scaledCellHeight / 2 - totalHeight / 2 + lineHeight / 2

                    textLines.forEachIndexed { index, line ->
                        drawText(
                            line, x + width / 2, startY + index * lineHeight, paint
                        )
                    }
                }
            }
        }
    }
}

fun parsePeriods(classes: JSONArray, numDays: Int): List<Period> {
    val periods = mutableListOf<Period>()

    for (dayIndex in 0 until min(numDays, classes.length())) {
        val dayClasses = classes.optJSONArray(dayIndex) ?: continue

        var periodIndex = 0
        while (periodIndex < dayClasses.length()) {
            val periodName = dayClasses.optString(periodIndex, "").trim()

            if (periodName.isNotEmpty()) {
                var endPeriod = periodIndex
                while (endPeriod + 1 < dayClasses.length() && dayClasses.optString(
                        endPeriod + 1, ""
                    ).trim() == periodName
                ) {
                    endPeriod++
                }

                periods.add(
                    Period(
                        dayIndex = dayIndex,
                        startPeriod = periodIndex,
                        endPeriod = endPeriod,
                        name = periodName
                    )
                )

                periodIndex = endPeriod + 1
            } else {
                periodIndex++
            }
        }
    }

    return periods
}

fun getCurrentPeriodIndex(hours: JSONArray, currentTimeInMinutes: Int): Int {
    if (hours.length() < 2) return -1

    for (i in 0 until hours.length() - 1) {
        val startTime = hours.getString(i)
        val endTime = hours.getString(i + 1)

        val startMinutes = parseTimeToMinutes(startTime)
        val endMinutes = parseTimeToMinutes(endTime)

        if (currentTimeInMinutes in startMinutes..<endMinutes) {
            return i
        }
    }

    return -1
}

fun parseTimeToMinutes(timeString: String): Int {
    val parts = timeString.split(":")
    if (parts.size != 2) return 0

    val hours = parts[0].toIntOrNull() ?: 0
    val minutes = parts[1].toIntOrNull() ?: 0

    return hours * 60 + minutes
}

fun convertTo12Hour(time24: String): String {
    val parts = time24.split(":")
    if (parts.size != 2) return time24

    val hour24 = parts[0].toIntOrNull() ?: return time24
    val minute = parts[1]

    val period = if (hour24 >= 12) "PM" else "AM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }

    return "$hour12:$minute $period"
}
