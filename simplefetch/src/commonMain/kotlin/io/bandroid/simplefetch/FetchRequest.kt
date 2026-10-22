package io.bandroid.simplefetch

/**
 * Encapsulates parameters for a network call.
 */
data class FetchRequest(
    val url: String,
    val method: HttpMethod = HttpMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val queryParams: Map<String, Any?> = emptyMap(),
    val body: Any? = null,
    val connectTimeoutMs: Int = 15000,
    val readTimeoutMs: Int = 15000
) {
    /**
     * Constructs full URL including encoded query parameters.
     */
    fun buildFullUrl(): String {
        if (queryParams.isEmpty()) return url
        val queryString = queryParams.entries.joinToString("&") { (key, value) ->
            val encodedKey = encodeUrlComponent(key)
            val encodedVal = encodeUrlComponent(value?.toString() ?: "")
            "$encodedKey=$encodedVal"
        }
        val separator = if (url.contains("?")) "&" else "?"
        return "$url$separator$queryString"
    }

    companion object {
        fun encodeUrlComponent(value: String): String {
            val allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~"
            val bytes = value.encodeToByteArray()
            val sb = StringBuilder()
            for (b in bytes) {
                val ch = (b.toInt() and 0xFF).toChar()
                if (ch in allowed) {
                    sb.append(ch)
                } else {
                    sb.append('%')
                    val hex = (b.toInt() and 0xFF).toString(16).uppercase()
                    if (hex.length == 1) sb.append('0')
                    sb.append(hex)
                }
            }
            return sb.toString()
        }
    }

    /**
     * Formats the body as a string. If the body is a Map or List, it converts it to JSON.
     */
    fun bodyAsString(): String? {
        return when (val b = body) {
            null -> null
            is String -> b
            is Map<*, *> -> SimpleJson.stringify(b)
            is List<*> -> SimpleJson.stringify(b)
            is Number, is Boolean -> b.toString()
            else -> SimpleJson.stringify(b)
        }
    }
}

/**
 * DSL Builder for creating a [FetchRequest].
 */
class FetchRequestBuilder {
    var url: String = ""
    var method: HttpMethod = HttpMethod.GET
    var body: Any? = null
    var connectTimeoutMs: Int = 15000
    var readTimeoutMs: Int = 15000

    private val headersMap = LinkedHashMap<String, String>()
    private val queryMap = LinkedHashMap<String, Any?>()

    fun header(key: String, value: String) {
        headersMap[key] = value
    }

    fun headers(headers: Map<String, String>) {
        headersMap.putAll(headers)
    }

    fun query(key: String, value: Any?) {
        queryMap[key] = value
    }

    fun queries(params: Map<String, Any?>) {
        queryMap.putAll(params)
    }

    fun build(): FetchRequest {
        require(url.isNotBlank()) { "URL cannot be blank" }
        return FetchRequest(
            url = url,
            method = method,
            headers = headersMap,
            queryParams = queryMap,
            body = body,
            connectTimeoutMs = connectTimeoutMs,
            readTimeoutMs = readTimeoutMs
        )
    }
}
