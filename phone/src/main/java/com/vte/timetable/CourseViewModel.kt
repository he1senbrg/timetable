package com.vte.timetable

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class CourseViewModel : ViewModel() {
    val finalCourseList = mutableStateListOf<String>()
}