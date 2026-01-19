package com.vte.timetable.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun TimeTableTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}