package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CodeSnippetCard
import com.example.ui.components.StatusBadge
import io.bandroid.simplefetch.FetchState
import io.bandroid.simplefetch.fetchAsState
import io.bandroid.simplefetch.getBoolean
import io.bandroid.simplefetch.getString
import io.bandroid.simplefetch.rememberFetchController

@Composable
fun ComposeDemosScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Compose Multiplatform Patterns",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Initiate and observe requests in a single declarative block",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Demo 1: fetchAsState
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pattern 1: fetchAsState (Zero-Boilerplate Observer)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "A single line initiates the network call and automatically binds its state to Compose recomposition.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Interactive Counter Stepper
                var todoId by remember { mutableIntStateOf(1) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Todo Record #$todoId",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (todoId > 1) todoId-- },
                            enabled = todoId > 1,
                            modifier = Modifier.testTag("prev_todo_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                        }
                        IconButton(
                            onClick = { if (todoId < 20) todoId++ },
                            enabled = todoId < 20,
                            modifier = Modifier.testTag("next_todo_button")
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                        }
                    }
                }

                // THE SINGLE BLOCK CALL
                val todoUrl = "https://jsonplaceholder.typicode.com/todos/$todoId"
                val fetchState by fetchAsState(url = todoUrl, key = todoId)

                // Live Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    when (val s = fetchState) {
                        is FetchState.Idle -> {
                            Text("Ready to fetch...", fontSize = 12.sp)
                        }
                        is FetchState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text("Fetching Todo #$todoId...", fontSize = 12.sp)
                            }
                        }
                        is FetchState.Success -> {
                            val map = s.result.jsonMap
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadge(s.result.statusCode, s.result.durationMs)
                                    val isCompleted = map.getBoolean("completed")
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isCompleted) Color(0xFF10B981).copy(alpha = 0.15f)
                                                else Color(0xFFEF4444).copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isCompleted) "COMPLETED" else "PENDING",
                                            color = if (isCompleted) Color(0xFF10B981) else Color(0xFFEF4444),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = map.getString("title"),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Access via: map.getString(\"title\") / map[\"title\"]",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        is FetchState.Error -> {
                            Text("Failed: ${s.message}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }

                CodeSnippetCard(
                    title = "Single-Block Composable Implementation",
                    code = """
@Composable
fun TodoCard(todoId: Int) {
    // 1-Line Fetch & Reactive Observation:
    val state by fetchAsState("https://api.example.com/todos/" + todoId, key = todoId)

    when (val s = state) {
        is FetchState.Loading -> CircularProgressIndicator()
        is FetchState.Success -> Text("Title: " + s.result.jsonMap["title"])
        is FetchState.Error -> Text("Error: " + s.message)
        else -> {}
    }
}
""".trimIndent()
                )
            }
        }

        // Demo 2: rememberFetchController for Interactive Actions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pattern 2: rememberFetchController (Form Submission)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Execute POST, PUT, DELETE operations on user events with Map payloads.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                var postTitle by remember { mutableStateOf("New Post via SimpleFetch") }
                var postBody by remember { mutableStateOf("Created at ${System.currentTimeMillis()}") }

                OutlinedTextField(
                    value = postTitle,
                    onValueChange = { postTitle = it },
                    label = { Text("Post Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = postBody,
                    onValueChange = { postBody = it },
                    label = { Text("Post Body") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val controller = rememberFetchController()

                Button(
                    onClick = {
                        val payload = mapOf(
                            "title" to postTitle,
                            "body" to postBody,
                            "userId" to 1,
                            "createdAt" to System.currentTimeMillis()
                        )
                        controller.post("https://jsonplaceholder.typicode.com/posts", body = payload)
                    },
                    enabled = !controller.state.value.isLoading && postTitle.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_post_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (controller.state.value.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Submitting POST...")
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Submit POST with Map Payload")
                    }
                }

                // Controller State Card
                when (val state = controller.state.value) {
                    is FetchState.Success -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Text("Post Created! HTTP ${state.result.statusCode}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp)
                                }
                                Text("Assigned ID: ${state.result.jsonMap["id"]}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Echoed Title: ${state.result.jsonMap["title"]}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    is FetchState.Error -> {
                        Text("Submission failed: ${state.message}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    else -> {}
                }

                CodeSnippetCard(
                    title = "Controller Usage Snippet",
                    code = """
val controller = rememberFetchController()

Button(onClick = {
    controller.post(
        url = "https://api.example.com/posts",
        body = mapOf("title" to title, "userId" to 1)
    )
}) {
    Text("Submit")
}
""".trimIndent()
                )
            }
        }
    }
}
