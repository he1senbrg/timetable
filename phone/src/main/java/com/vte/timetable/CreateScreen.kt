package com.vte.timetable

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.json.JSONObject

@Composable
fun CreateScreen(
    textNow: String, navController: NavController, context: Context, bottomNavPadding: PaddingValues
) {
    var jsonText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val existingJson = readConfigFromFile(context, "time-table.json")
        if (existingJson != null) {
            jsonText = existingJson.toString(2)
        } else {
            jsonText = """{"hours": [], "days": [], "classes": [[], [], [], [], [], [], []]}"""
        }
    }

    Scaffold(
        topBar = { TopBarCreate(textNow, navController = navController) },
        floatingActionButton = {
            SubmitButton(
                context = context,
                navController = navController,
                jsonText = jsonText,
                onError = { errorMessage = it },
                bottomPadding = bottomNavPadding
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        CreateContent(
            innerPadding = innerPadding,
            bottomNavPadding = bottomNavPadding,
            jsonText = jsonText,
            onJsonTextChange = { jsonText = it },
            errorMessage = errorMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarCreate(textNow: String, navController: NavController) {
    TopAppBar(title = { Text(text = textNow) }, navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
    })
}


@Composable
fun CreateContent(
    innerPadding: PaddingValues,
    bottomNavPadding: PaddingValues,
    jsonText: String,
    onJsonTextChange: (String) -> Unit,
    errorMessage: String
) {
    Box(
        modifier = Modifier
            .padding(innerPadding)
            .padding(bottomNavPadding)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 72.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Timetable JSON",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Enter your timetable in JSON format. Expected structure:\n{\"hours\": [...], \"days\": [...], \"classes\": [[...], ...]}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = jsonText,
                onValueChange = onJsonTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("JSON Content") },
                placeholder = { Text("""{"hours": [], "days": [], "classes": [[],[],[],[],[],[],[]]}""") },
                isError = errorMessage.isNotEmpty(),
                supportingText = if (errorMessage.isNotEmpty()) {
                    { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                } else null,
                maxLines = Int.MAX_VALUE)
        }
    }
}

@Composable
fun SubmitButton(
    context: Context,
    navController: NavController,
    jsonText: String,
    onError: (String) -> Unit,
    bottomPadding: PaddingValues
) {
    FloatingActionButton(
        onClick = {
            try {
                val jsonObject = JSONObject(jsonText)

                if (!jsonObject.has("hours") || !jsonObject.has("days") || !jsonObject.has("classes")) {
                    onError("Invalid JSON: Missing required fields (hours, days, classes)")
                    Toast.makeText(context, "Invalid JSON structure", Toast.LENGTH_SHORT).show()
                    return@FloatingActionButton
                }

                saveConfigToFile(context, "time-table.json", jsonObject)

                sendMessageToWear(context)

                Toast.makeText(context, "Timetable saved!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } catch (e: Exception) {
                Log.e("CreateScreen", "Invalid JSON", e)
                onError("Invalid JSON: ${e.message}")
                Toast.makeText(context, "Invalid JSON format", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.padding(bottom = 80.dp)
    ) {
        Icon(Icons.Rounded.Check, contentDescription = "Submit")
    }
}