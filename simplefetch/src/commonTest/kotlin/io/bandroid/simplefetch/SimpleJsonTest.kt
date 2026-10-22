package io.bandroid.simplefetch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleJsonTest {

    @Test
    fun testParseSimpleObject() {
        val json = """{"name": "Alice", "age": 30, "active": true, "score": 98.5}"""
        val parsed = SimpleJson.parseObject(json)

        assertEquals("Alice", parsed["name"])
        assertEquals(30L, parsed["age"])
        assertEquals(true, parsed["active"])
        assertEquals(98.5, parsed["score"])
    }

    @Test
    fun testParseNestedObjectAndPath() {
        val json = """
            {
              "id": 101,
              "user": {
                "profile": {
                  "firstName": "John",
                  "lastName": "Doe"
                },
                "tags": ["admin", "developer"]
              }
            }
        """.trimIndent()

        val map = SimpleJson.parseObject(json)
        assertEquals("John", map.path("user.profile.firstName"))
        assertEquals("Doe", map.path("user.profile.lastName"))
        assertNull(map.path("user.profile.unknown"))

        val tags = map.getArray("user.tags") ?: (map.getObject("user")?.getArray("tags"))
        assertNotNull(tags)
        assertEquals(2, tags?.size)
        assertEquals("admin", tags?.get(0))
    }

    @Test
    fun testStringifyObject() {
        val map = linkedMapOf<String, Any?>(
            "title" to "Test Post",
            "count" to 5,
            "published" to false,
            "items" to listOf("a", "b")
        )

        val json = SimpleJson.stringify(map)
        assertTrue(json.contains("\"title\": \"Test Post\"") || json.contains("\"title\":\"Test Post\""))
        assertTrue(json.contains("\"count\": 5") || json.contains("\"count\":5"))
    }

    @Test
    fun testExtensionHelpers() {
        val map = mapOf<String, Any?>(
            "str" to "Hello",
            "num" to 42,
            "flag" to true,
            "nested" to mapOf("inner" to "world")
        )

        assertEquals("Hello", map.getString("str"))
        assertEquals(42, map.getInt("num"))
        assertEquals(42L, map.getLong("num"))
        assertEquals(true, map.getBoolean("flag"))
        assertEquals("default", map.getString("missing", "default"))
        assertEquals("world", map.getObject("nested")?.getString("inner"))
    }

    @Test
    fun testEscapedCharacters() {
        val json = """{"message": "Hello \"World\"\nLine 2"}"""
        val map = SimpleJson.parseObject(json)
        assertEquals("Hello \"World\"\nLine 2", map["message"])
    }
}
