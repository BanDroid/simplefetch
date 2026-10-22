package io.bandroid.simplefetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Controller to trigger and observe network calls directly from Jetpack Compose / Compose Multiplatform UI.
 */
@Stable
class FetchController(private val coroutineScope: CoroutineScope) {

    private val _state = mutableStateOf<FetchState>(FetchState.Idle)
    val state: State<FetchState> get() = _state

    private var currentJob: Job? = null

    /**
     * Executes a request and updates [state] reactively.
     */
    fun execute(request: FetchRequest): Job {
        currentJob?.cancel()
        currentJob = coroutineScope.launch {
            SimpleFetch.fetchFlow(request).collect { newState ->
                _state.value = newState
            }
        }
        return currentJob!!
    }

    /**
     * Shorthand single block fetch call.
     */
    fun fetch(
        url: String,
        method: HttpMethod = HttpMethod.GET,
        body: Any? = null,
        params: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): Job {
        val request = FetchRequest(
            url = url,
            method = method,
            body = body,
            queryParams = params,
            headers = headers
        )
        return execute(request)
    }

    fun get(url: String, params: Map<String, Any?> = emptyMap(), headers: Map<String, String> = emptyMap()): Job {
        return fetch(url, HttpMethod.GET, null, params, headers)
    }

    fun post(url: String, body: Any? = null, params: Map<String, Any?> = emptyMap(), headers: Map<String, String> = emptyMap()): Job {
        return fetch(url, HttpMethod.POST, body, params, headers)
    }

    fun put(url: String, body: Any? = null, params: Map<String, Any?> = emptyMap(), headers: Map<String, String> = emptyMap()): Job {
        return fetch(url, HttpMethod.PUT, body, params, headers)
    }

    fun delete(url: String, params: Map<String, Any?> = emptyMap(), headers: Map<String, String> = emptyMap(), body: Any? = null): Job {
        return fetch(url, HttpMethod.DELETE, body, params, headers)
    }

    fun reset() {
        currentJob?.cancel()
        _state.value = FetchState.Idle
    }
}

/**
 * Creates and remembers a [FetchController] within the Composable scope.
 */
@Composable
fun rememberFetchController(): FetchController {
    val scope = rememberCoroutineScope()
    return remember(scope) { FetchController(scope) }
}

/**
 * Initiates and observes a network request in a single Composable declaration:
 * ```
 * val fetchState by fetchAsState(url = "https://jsonplaceholder.typicode.com/posts/1")
 * ```
 */
@Composable
fun fetchAsState(
    url: String,
    method: HttpMethod = HttpMethod.GET,
    body: Any? = null,
    params: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
    key: Any? = url,
    enabled: Boolean = true
): State<FetchState> {
    val state = remember(key) { mutableStateOf<FetchState>(FetchState.Idle) }

    LaunchedEffect(key, enabled) {
        if (!enabled || url.isBlank()) return@LaunchedEffect
        val request = FetchRequest(
            url = url,
            method = method,
            body = body,
            queryParams = params,
            headers = headers
        )
        SimpleFetch.fetchFlow(request).collect { newState ->
            state.value = newState
        }
    }

    return state
}

/**
 * Convenient tuple returning current [FetchState] and a trigger lambda for button actions.
 */
@Composable
fun rememberFetchLauncher(): Pair<FetchState, (url: String, method: HttpMethod, body: Any?, params: Map<String, Any?>, headers: Map<String, String>) -> Job> {
    val controller = rememberFetchController()
    val trigger = remember(controller) {
        { url: String, method: HttpMethod, body: Any?, params: Map<String, Any?>, headers: Map<String, String> ->
            controller.fetch(url, method, body, params, headers)
        }
    }
    return Pair(controller.state.value, trigger)
}

