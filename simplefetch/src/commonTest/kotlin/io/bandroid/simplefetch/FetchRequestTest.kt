package io.bandroid.simplefetch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchRequestTest {

    @Test
    fun testBuildFullUrlWithQueryParams() {
        val request = FetchRequest(
            url = "https://api.example.com/items",
            queryParams = mapOf("limit" to 10, "filter" to "active state")
        )

        val fullUrl = request.buildFullUrl()
        assertTrue(fullUrl.startsWith("https://api.example.com/items?"))
        assertTrue(fullUrl.contains("limit=10"))
        assertTrue(fullUrl.contains("filter=active+state") || fullUrl.contains("filter=active%20state"))
    }

    @Test
    fun testBodySerialization() {
        val bodyMap = mapOf("name" to "Sample", "price" to 99)
        val request = FetchRequest(
            url = "https://api.example.com/items",
            method = HttpMethod.POST,
            body = bodyMap
        )

        val bodyString = request.bodyAsString()
        assertTrue(bodyString != null)
        assertTrue(bodyString!!.contains("\"name\""))
        assertTrue(bodyString.contains("\"Sample\""))
    }

    @Test
    fun testDslBuilder() {
        val request = FetchRequestBuilder().apply {
            url = "https://api.example.com/users"
            method = HttpMethod.PUT
            header("Authorization", "Bearer token123")
            query("active", true)
            body = mapOf("role" to "admin")
        }.build()

        assertEquals("https://api.example.com/users", request.url)
        assertEquals(HttpMethod.PUT, request.method)
        assertEquals("Bearer token123", request.headers["Authorization"])
        assertEquals(true, request.queryParams["active"])
    }
}
