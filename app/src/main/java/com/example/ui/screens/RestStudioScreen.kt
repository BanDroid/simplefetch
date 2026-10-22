package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.components.MethodBadge
import com.example.ui.components.StatusBadge
import io.bandroid.simplefetch.FetchRequest
import io.bandroid.simplefetch.FetchResult
import io.bandroid.simplefetch.FetchState
import io.bandroid.simplefetch.HttpMethod
import io.bandroid.simplefetch.SimpleFetch
import io.bandroid.simplefetch.SimpleJson
import io.bandroid.simplefetch.path
import kotlinx.coroutines.launch

data class PresetEndpoint(
    val title: String,
    val method: HttpMethod,
    val url: String,
    val bodyJson: String? = null,
    val params: Map<String, String> = emptyMap()
)

data class KeyValuePair(val id: Int, var key: String, var value: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestStudioScreen() {
    val scope = rememberCoroutineScope()

    val presets = remember {
        listOf(
            PresetEndpoint(
                title = "GET Post (JSON Object)",
                method = HttpMethod.GET,
                url = "https://jsonplaceholder.typicode.com/posts/1"
            ),
            PresetEndpoint(
                title = "GET Todos (JSON Array)",
                method = HttpMethod.GET,
                url = "https://jsonplaceholder.typicode.com/todos",
                params = mapOf("_limit" to "5")
            ),
            PresetEndpoint(
                title = "POST Create (JSON Map)",
                method = HttpMethod.POST,
                url = "https://jsonplaceholder.typicode.com/posts",
                bodyJson = """{"title": "SimpleFetch Multiplatform", "body": "Pure Kotlin zero-boilerplate HTTP library", "userId": 1}"""
            ),
            PresetEndpoint(
                title = "PUT Update (JSON Map)",
                method = HttpMethod.PUT,
                url = "https://jsonplaceholder.typicode.com/posts/1",
                bodyJson = """{"id": 1, "title": "Updated Title via SimpleFetch", "body": "Updated body payload", "userId": 1}"""
            ),
            PresetEndpoint(
                title = "DELETE Post",
                method = HttpMethod.DELETE,
                url = "https://jsonplaceholder.typicode.com/posts/1"
            ),
            PresetEndpoint(
                title = "Simulate 404 (Error Handling)",
                method = HttpMethod.GET,
                url = "https://httpbin.org/status/404"
            ),
            PresetEndpoint(
                title = "Plain Text (Robots.txt)",
                method = HttpMethod.GET,
                url = "https://httpbin.org/robots.txt"
            )
        )
    }

    var selectedMethod by remember { mutableStateOf(HttpMethod.GET) }
    var urlInput by remember { mutableStateOf("https://jsonplaceholder.typicode.com/posts/1") }
    var rawBodyInput by remember { mutableStateOf("") }
    var isMapBodyMode by remember { mutableStateOf(true) }

    val queryParams = remember { mutableStateListOf<KeyValuePair>() }
    val headerPairs = remember { mutableStateListOf<KeyValuePair>() }
    val bodyPairs = remember {
        mutableStateListOf(
            KeyValuePair(1, "title", "SimpleFetch Launch"),
            KeyValuePair(2, "userId", "1"),
            KeyValuePair(3, "category", "Mobile Dev")
        )
    }

    var nextId by remember { mutableIntStateOf(10) }
    var showPresetMenu by remember { mutableStateOf(false) }
    var showParamsSection by remember { mutableStateOf(false) }
    var showHeadersSection by remember { mutableStateOf(false) }

    // State of execution
    var fetchState by remember { mutableStateOf<FetchState>(FetchState.Idle) }
    var responseTab by remember { mutableIntStateOf(0) }
    var testPathQuery by remember { mutableStateOf("title") }

    fun applyPreset(preset: PresetEndpoint) {
        selectedMethod = preset.method
        urlInput = preset.url
        queryParams.clear()
        preset.params.forEach { (k, v) ->
            queryParams.add(KeyValuePair(nextId++, k, v))
        }
        if (preset.bodyJson != null) {
            rawBodyInput = preset.bodyJson
            isMapBodyMode = false
        } else {
            rawBodyInput = ""
        }
    }

    fun executeCall() {
        scope.launch {
            val qMap = queryParams.filter { it.key.isNotBlank() }.associate { it.key to it.value }
            val hMap = headerPairs.filter { it.key.isNotBlank() }.associate { it.key to it.value }

            val payload: Any? = if (selectedMethod.requiresBody || selectedMethod == HttpMethod.DELETE) {
                if (isMapBodyMode) {
                    val m = bodyPairs.filter { it.key.isNotBlank() }.associate { pair ->
                        val intVal = pair.value.toLongOrNull()
                        val boolVal = if (pair.value.equals("true", true)) true else if (pair.value.equals("false", true)) false else null
                        pair.key to (intVal ?: boolVal ?: pair.value)
                    }
                    if (m.isNotEmpty()) m else null
                } else {
                    if (rawBodyInput.isNotBlank()) rawBodyInput else null
                }
            } else null

            val req = FetchRequest(
                url = urlInput.trim(),
                method = selectedMethod,
                queryParams = qMap,
                headers = hMap,
                body = payload
            )

            SimpleFetch.fetchFlow(req).collect { state ->
                fetchState = state
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SimpleFetch REST Studio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Lightweight zero-abstraction HTTP engine with untyped Map support",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Quick Preset Picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preset Endpoints",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Box {
                OutlinedButton(
                    onClick = { showPresetMenu = true },
                    modifier = Modifier.testTag("preset_dropdown_button")
                ) {
                    Text("Select Preset", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                DropdownMenu(
                    expanded = showPresetMenu,
                    onDismissRequest = { showPresetMenu = false }
                ) {
                    presets.forEach { preset ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MethodBadge(preset.method)
                                    Text(preset.title, fontSize = 13.sp)
                                }
                            },
                            onClick = {
                                applyPreset(preset)
                                showPresetMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Method Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH).forEach { method ->
                MethodBadge(
                    method = method,
                    isSelected = selectedMethod == method,
                    onClick = { selectedMethod = method },
                    modifier = Modifier.testTag("method_${method.name.lowercase()}")
                )
            }
        }

        // URL Input
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Target URL") },
            placeholder = { Text("https://api.example.com/data") },
            trailingIcon = {
                if (urlInput.isNotEmpty()) {
                    IconButton(onClick = { urlInput = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear URL")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("url_input_field")
        )

        // Query Parameters Section Toggle
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showParamsSection = !showParamsSection },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Query Parameters (${queryParams.size})",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = if (showParamsSection) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }

                if (showParamsSection) {
                    Spacer(Modifier.height(8.dp))
                    queryParams.forEachIndexed { index, pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pair.key,
                                onValueChange = { pair.key = it; queryParams[index] = pair.copy() },
                                label = { Text("Key", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = pair.value,
                                onValueChange = { pair.value = it; queryParams[index] = pair.copy() },
                                label = { Text("Value", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = { queryParams.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { queryParams.add(KeyValuePair(nextId++, "", "")) },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Query Param", fontSize = 12.sp)
                    }
                }
            }
        }

        // Custom Headers Section Toggle
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHeadersSection = !showHeadersSection },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Headers (${headerPairs.size})",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = if (showHeadersSection) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }

                if (showHeadersSection) {
                    Spacer(Modifier.height(8.dp))
                    headerPairs.forEachIndexed { index, pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pair.key,
                                onValueChange = { pair.key = it; headerPairs[index] = pair.copy() },
                                label = { Text("Header", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = pair.value,
                                onValueChange = { pair.value = it; headerPairs[index] = pair.copy() },
                                label = { Text("Value", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = { headerPairs.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { headerPairs.add(KeyValuePair(nextId++, "", "")) },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Header", fontSize = 12.sp)
                    }
                }
            }
        }

        // Request Body Section (for methods with payload)
        if (selectedMethod.requiresBody || selectedMethod == HttpMethod.DELETE) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Request Body", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = isMapBodyMode,
                                onClick = { isMapBodyMode = true },
                                label = { Text("Key-Value Map", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = !isMapBodyMode,
                                onClick = { isMapBodyMode = false },
                                label = { Text("Raw JSON / Text", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (isMapBodyMode) {
                        bodyPairs.forEachIndexed { index, pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = pair.key,
                                    onValueChange = { pair.key = it; bodyPairs[index] = pair.copy() },
                                    label = { Text("Key", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = pair.value,
                                    onValueChange = { pair.value = it; bodyPairs[index] = pair.copy() },
                                    label = { Text("Value", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                IconButton(onClick = { bodyPairs.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { bodyPairs.add(KeyValuePair(nextId++, "", "")) },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Field to Map", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedTextField(
                            value = rawBodyInput,
                            onValueChange = { rawBodyInput = it },
                            label = { Text("Raw JSON or Text") },
                            placeholder = { Text("""{"key": "value"}""") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            }
        }

        // Send Request Primary Button
        Button(
            onClick = { executeCall() },
            enabled = !fetchState.isLoading && urlInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("send_request_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (fetchState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Fetching Network Data...", fontSize = 15.sp)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send ${selectedMethod.name} Request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        // Response Section
        val result = fetchState.resultOrNull
        if (fetchState.isLoading) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Connecting to server...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (result != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(result.statusCode, result.durationMs)
                        Text(
                            text = result.method.name,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    if (result.errorMessage != null && !result.isSuccess) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Error: ${result.errorMessage}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Tab selector
                    val tabs = listOf("Dynamic Map", "Formatted JSON", "Raw Output", "Headers", "Kotlin Code")
                    ScrollableTabRow(
                        selectedTabIndex = responseTab,
                        edgePadding = 0.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = responseTab == index,
                                onClick = { responseTab = index },
                                text = { Text(title, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    when (responseTab) {
                        0 -> {
                            // Dynamic Key-Value Map Inspector
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Untyped Map<String, Any?> Access",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "SimpleFetch converts JSON directly to standard Kotlin Maps without model classes.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (result.isJsonObject) {
                                    val map = result.jsonMap
                                    // Interactive Path Query Tester
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = testPathQuery,
                                            onValueChange = { testPathQuery = it },
                                            label = { Text("Query path e.g. 'title' or 'nested.key'", fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    val resolvedValue = map.path(testPathQuery) ?: map[testPathQuery]
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "result.jsonMap[\"$testPathQuery\"] = $resolvedValue",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(Modifier.height(4.dp))
                                    Text("Top-level Key-Value pairs:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                                    map.entries.take(15).forEach { (k, v) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(k, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        v?.javaClass?.simpleName ?: "null",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            SelectionContainer {
                                                Text(
                                                    text = v?.toString()?.take(60) ?: "null",
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    if (map.size > 15) {
                                        Text("+ ${map.size - 15} more entries", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                } else if (result.isJsonArray) {
                                    val list = result.jsonList
                                    Text("Received JSON Array with ${list.size} items", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    list.take(5).forEachIndexed { i, item ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                .padding(8.dp)
                                        ) {
                                            SelectionContainer {
                                                Text(
                                                    text = "[$i]: ${item.toString().take(100)}",
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text("Response is raw text / non-JSON", fontSize = 12.sp)
                                }
                            }
                        }

                        1 -> {
                            // Formatted JSON
                            SelectionContainer {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = result.formattedBody().ifEmpty { "<Empty Body>" },
                                        color = Color(0xFFE2E8F0),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Raw Text Output
                            SelectionContainer {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = result.text.ifEmpty { "<No Text Content>" },
                                        color = Color(0xFFE2E8F0),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        3 -> {
                            // Headers Table
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                result.headers.filter { it.key != null }.forEach { (key, vals) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(key, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        SelectionContainer {
                                            Text(vals.joinToString(", "), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        4 -> {
                            // Kotlin Code Snippet Generator
                            val codeSnippet = when (selectedMethod) {
                                HttpMethod.GET -> """
// 1. Direct Coroutine Call:
val result = SimpleFetch.get("${result.url}")
val data: Map<String, Any?> = result.jsonMap
println("Title: " + data["title"])

// 2. Compose Multiplatform State Observer:
val fetchState by fetchAsState("${result.url}")
when (val s = fetchState) {
    is FetchState.Loading -> CircularProgressIndicator()
    is FetchState.Success -> Text(s.result.jsonMap.getString("title"))
    is FetchState.Error -> Text("Failed: " + s.message)
    else -> {}
}
""".trimIndent()
                                HttpMethod.POST -> """
// 1. Initiate and send dynamic Map payload:
val payload = mapOf(
    "title" to "SimpleFetch",
    "userId" to 1
)
val result = SimpleFetch.post("${result.url}", body = payload)
if (result.isSuccess) {
    val createdId = result.jsonMap["id"]
}

// 2. In Compose UI Event:
val fetcher = rememberFetchController()
Button(onClick = { fetcher.post("${result.url}", body = payload) }) {
    Text("Submit")
}
""".trimIndent()
                                HttpMethod.PUT -> """
// PUT update record:
val result = SimpleFetch.put(
    url = "${result.url}",
    body = mapOf("title" to "Updated Title")
)
""".trimIndent()
                                HttpMethod.DELETE -> """
// DELETE record:
val result = SimpleFetch.delete("${result.url}")
if (result.isSuccess) {
    println("Deleted successfully")
}
""".trimIndent()
                                else -> """
val result = SimpleFetch.fetch {
    url = "${result.url}"
    method = HttpMethod.${result.method.name}
}
""".trimIndent()
                            }

                            CodeSnippetCard(
                                title = "Kotlin / Compose Multiplatform Integration",
                                code = codeSnippet
                            )
                        }
                    }
                }
            }
        }
    }
}
