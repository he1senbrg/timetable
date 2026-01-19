package com.vte.timetable

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Calendar

@Composable
fun ManageScreen(navController: NavController, context: Context, padding: PaddingValues) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
        ) {
            ManageCard(
                navController = navController,
                context = context,
                snackbarHostState = snackbarHostState,
                scope = scope
            )
        }

        FloatingActionButton(
            onClick = { navController.navigate("create?textNow=Edit Timetable") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTopBar() {
    TopAppBar(title = { Text(text = "Manage Timetable") })
}

@Composable
fun ManageCard(
    navController: NavController,
    context: Context,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val days =
        listOf("None", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDay by remember { mutableStateOf("") }
    val dummyJSON =
        JSONObject("{\"hours\":[],\"days\":[],\"classes\":[[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"],[\"\"]]}")

    val calendar = Calendar.getInstance()
    val currentDayInt = calendar.get(Calendar.DAY_OF_WEEK)
    val currentDay = dayMappingString(currentDayInt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Current Day: $currentDay",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Clone Timetable",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OptionBox(
                options = days, boxLabel = "Clone from", onValueChange = {
                    selectedDay = it
                    cloneAnotherDayToday(it, context)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Cloned from $it",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }
                })

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    saveConfigToFile(context, "time-table.json", dummyJSON)
                    sendMessageToWear(context)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Timetable deleted",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Delete Timetable")
            }
        }
    }
}
