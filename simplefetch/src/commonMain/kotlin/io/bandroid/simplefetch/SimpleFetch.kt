package io.bandroid.simplefetch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Global configuration options for SimpleFetch.
 */
class SimpleFetchConfig {
    var defaultBaseUrl: String = ""
    var defaultConnectTimeoutMs: Int = 15000
    var defaultReadTimeoutMs: Int = 15000
    val defaultHeaders = mutableMapOf<String, String>()

    fun header(key: String, value: String) {
        defaultHeaders[key] = value
    }
}

/**
 * Pluggable HTTP engine interface for multiplatform environments.
 * Allows custom engines (such as Ktor, Darwin on iOS, Wasm fetch, or mock engines).
 */
interface HttpEngine {
    suspend fun execute(request: FetchRequest, config: SimpleFetchConfig): FetchResult
}

/**
 * Default standard engine that works out-of-the-box on Android, JVM, and Compose Desktop.
 */
class DefaultHttpEngine : HttpEngine {
    override suspend fun execute(request: FetchRequest, config: SimpleFetchConfig): FetchResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var connection: HttpURLConnection? = null

        val finalUrl = if (config.defaultBaseUrl.isNotBlank() && !request.url.startsWith("http://") && !request.url.startsWith("https://")) {
            val base = config.defaultBaseUrl.removeSuffix("/")
            val path = request.url.removePrefix("/")
            "$base/$path"
        } else {
            request.url
        }

        val fullRequest = request.copy(url = finalUrl)
        val targetUrl = fullRequest.buildFullUrl()

        try {
            val urlObj = URL(targetUrl)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                connectTimeout = fullRequest.connectTimeoutMs.coerceAtLeast(config.defaultConnectTimeoutMs)
                readTimeout = fullRequest.readTimeoutMs.coerceAtLeast(config.defaultReadTimeoutMs)
                instanceFollowRedirects = true

                // Apply HTTP method
                requestMethod = fullRequest.method.name

                // Apply default headers
                config.defaultHeaders.forEach { (k, v) ->
                    setRequestProperty(k, v)
                }

                // Default Accept header
                if (getRequestProperty("Accept") == null) {
                    setRequestProperty("Accept", "application/json, text/plain, */*")
                }

                // Apply custom headers
                fullRequest.headers.forEach { (k, v) ->
                    setRequestProperty(k, v)
                }

                val bodyStr = fullRequest.bodyAsString()
                val hasBody = !bodyStr.isNullOrEmpty()

                if (fullRequest.method.requiresBody || hasBody) {
                    doOutput = true
                    if (getRequestProperty("Content-Type") == null) {
                        val isJsonBody = fullRequest.body is Map<*, *> || fullRequest.body is List<*> ||
                                (bodyStr != null && (bodyStr.startsWith("{") || bodyStr.startsWith("[")))
                        setRequestProperty(
                            "Content-Type",
                            if (isJsonBody) "application/json; charset=UTF-8" else "text/plain; charset=UTF-8"
                        )
                    }
                }
            }

            // Write request payload if present
            val bodyStr = fullRequest.bodyAsString()
            if (connection.doOutput && bodyStr != null) {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(bodyStr)
                    writer.flush()
                }
            }

            val statusCode = connection.responseCode
            val responseHeaders = connection.headerFields ?: emptyMap()

            // Read response stream (handles 2xx as well as 4xx/5xx error streams)
            val stream: InputStream? = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseText = stream?.use { readStream(it) } ?: ""
            val duration = System.currentTimeMillis() - startTime

            FetchResult(
                statusCode = statusCode,
                text = responseText,
                headers = responseHeaders,
                durationMs = duration,
                error = null,
                url = targetUrl,
                method = fullRequest.method
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            FetchResult(
                statusCode = connection?.responseCode ?: 0,
                text = "",
                headers = connection?.headerFields ?: emptyMap(),
                durationMs = duration,
                error = e,
                url = targetUrl,
                method = fullRequest.method
            )
        } finally {
            connection?.disconnect()
        }
    }

    private fun readStream(stream: InputStream): String {
        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }
}

/**
 * Main entry point for the SimpleFetch library.
 * Designed for Compose Multiplatform with minimal dependencies and zero-boilerplate usage.
 */
object SimpleFetch {

    val config = SimpleFetchConfig()

    /**
     * Active HTTP engine. Defaults to [DefaultHttpEngine], but can be swapped
     * for custom engines (e.g. Ktor, Darwin, MockHttpEngine).
     */
    var engine: HttpEngine = DefaultHttpEngine()

    /**
     * Configure default settings such as baseUrl or authorization headers.
     */
    fun configure(block: SimpleFetchConfig.() -> Unit) {
        config.apply(block)
    }

    /**
     * Executes a network request asynchronously on the configured engine.
     */
    suspend fun execute(request: FetchRequest): FetchResult {
        return engine.execute(request, config)
    }

    /**
     * Executes a request using a DSL block:
     * ```
     * val result = SimpleFetch.fetch {
     *     url = "https://api.example.com/items"
     *     method = HttpMethod.POST
     *     body = mapOf("name" to "Widget", "price" to 19.99)
     * }
     * ```
     */
    suspend fun fetch(block: FetchRequestBuilder.() -> Unit): FetchResult {
        val request = FetchRequestBuilder().apply(block).build()
        return execute(request)
    }

    /**
     * Executes GET request directly.
     */
    suspend fun get(
        url: String,
        params: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): FetchResult {
        return execute(
            FetchRequest(
                url = url,
                method = HttpMethod.GET,
                queryParams = params,
                headers = headers
            )
        )
    }

    /**
     * Executes POST request with Map, List, or text body.
     */
    suspend fun post(
        url: String,
        body: Any? = null,
        params: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): FetchResult {
        return execute(
            FetchRequest(
                url = url,
                method = HttpMethod.POST,
                body = body,
                queryParams = params,
                headers = headers
            )
        )
    }

    /**
     * Executes PUT request with Map, List, or text body.
     */
    suspend fun put(
        url: String,
        body: Any? = null,
        params: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): FetchResult {
        return execute(
            FetchRequest(
                url = url,
                method = HttpMethod.PUT,
                body = body,
                queryParams = params,
                headers = headers
            )
        )
    }

    /**
     * Executes DELETE request.
     */
    suspend fun delete(
        url: String,
        params: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: Any? = null
    ): FetchResult {
        return execute(
            FetchRequest(
                url = url,
                method = HttpMethod.DELETE,
                queryParams = params,
                headers = headers,
                body = body
            )
        )
    }

    /**
     * Executes PATCH request with Map, List, or text body.
     */
    suspend fun patch(
        url: String,
        body: Any? = null,
        params: Map<String, Any?> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): FetchResult {
        return execute(
            FetchRequest(
                url = url,
                method = HttpMethod.PATCH,
                body = body,
                queryParams = params,
                headers = headers
            )
        )
    }

    /**
     * Returns a cold Flow emitting [FetchState.Loading] immediately,
     * followed by [FetchState.Success] or [FetchState.Error].
     */
    fun fetchFlow(request: FetchRequest): Flow<FetchState> = flow {
        emit(FetchState.Loading(url = request.url, method = request.method))
        val result = execute(request)
        if (result.isSuccess) {
            emit(FetchState.Success(result))
        } else {
            val message = result.errorMessage ?: "Request failed with code ${result.statusCode}"
            emit(FetchState.Error(message = message, result = result, cause = result.error))
        }
    }

    /**
     * DSL Flow helper.
     */
    fun fetchFlow(block: FetchRequestBuilder.() -> Unit): Flow<FetchState> {
        val request = FetchRequestBuilder().apply(block).build()
        return fetchFlow(request)
    }

    /**
     * Single block helper to initiate a request and observe state transitions directly via a callback.
     */
    fun fetchLive(
        scope: CoroutineScope,
        request: FetchRequest,
        onStateChanged: (FetchState) -> Unit
    ) {
        scope.launch {
            fetchFlow(request).collect { state ->
                onStateChanged(state)
            }
        }
    }
}
