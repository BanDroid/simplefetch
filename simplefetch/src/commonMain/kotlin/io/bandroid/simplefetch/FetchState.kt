package io.bandroid.simplefetch

/**
 * Observable UI and coroutine state for network requests in SimpleFetch.
 */
sealed interface FetchState {
    /** Initial state before any request has been triggered. */
    object Idle : FetchState

    /** Active network request in progress. */
    data class Loading(
        val url: String = "",
        val method: HttpMethod = HttpMethod.GET,
        val startedAt: Long = System.currentTimeMillis()
    ) : FetchState

    /** Successful network response (HTTP 2xx). */
    data class Success(val result: FetchResult) : FetchState

    /** Failed network response (non-2xx HTTP code or network exception). */
    data class Error(
        val message: String,
        val result: FetchResult? = null,
        val cause: Throwable? = null
    ) : FetchState

    val isIdle: Boolean get() = this is Idle
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    val resultOrNull: FetchResult?
        get() = when (this) {
            is Success -> result
            is Error -> result
            else -> null
        }

    val jsonMapOrNull: Map<String, Any?>?
        get() = resultOrNull?.jsonMap
}
