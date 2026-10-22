package io.bandroid.simplefetch

/**
 * Standard HTTP methods supported by SimpleFetch.
 */
enum class HttpMethod(val requiresBody: Boolean = false) {
    GET(requiresBody = false),
    POST(requiresBody = true),
    PUT(requiresBody = true),
    DELETE(requiresBody = false),
    PATCH(requiresBody = true),
    HEAD(requiresBody = false);

    companion object {
        fun fromString(name: String): HttpMethod {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GET
        }
    }
}
