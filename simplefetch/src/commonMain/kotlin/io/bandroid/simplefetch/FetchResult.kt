package io.bandroid.simplefetch

/**
 * Result representation of an HTTP request executed by SimpleFetch.
 */
data class FetchResult(
    val statusCode: Int,
    val text: String = "",
    val headers: Map<String, List<String>> = emptyMap(),
    val durationMs: Long = 0L,
    val error: Throwable? = null,
    val url: String = "",
    val method: HttpMethod = HttpMethod.GET
) {
    /** True if the response HTTP status is within 200..299 and no exception occurred. */
    val isSuccess: Boolean get() = statusCode in 200..299 && error == null

    /** Lazy parsed untyped JSON (Map, List, Primitive, or null). */
    val json: Any? by lazy {
        try {
            SimpleJson.parse(text)
        } catch (_: Exception) {
            null
        }
    }

    /** Convenient Map representation of the JSON body (or empty map if not a JSON object). */
    @Suppress("UNCHECKED_CAST")
    val jsonMap: Map<String, Any?> get() = (json as? Map<String, Any?>) ?: emptyMap()

    /** Convenient List representation of the JSON body (or empty list if not a JSON array). */
    @Suppress("UNCHECKED_CAST")
    val jsonList: List<Any?> get() = (json as? List<Any?>) ?: emptyList()

    /** True if the body is a valid JSON payload. */
    val isJson: Boolean get() = json != null

    /** True if the body is a JSON object ({...}). */
    val isJsonObject: Boolean get() = json is Map<*, *>

    /** True if the body is a JSON array ([...]). */
    val isJsonArray: Boolean get() = json is List<*>

    /** Get the first value of a specific response header (case-insensitive). */
    fun header(name: String): String? {
        val entry = headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }
        return entry?.value?.firstOrNull()
    }

    /** Formatted human-readable JSON if valid, otherwise raw text. */
    fun formattedBody(): String {
        val parsed = json
        return if (parsed != null) {
            SimpleJson.stringify(parsed, pretty = true)
        } else {
            text
        }
    }

    val errorMessage: String?
        get() = error?.message ?: if (!isSuccess && statusCode > 0) "HTTP error $statusCode" else null
}
