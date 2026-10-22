package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.bandroid.simplefetch.FetchRequest
import io.bandroid.simplefetch.HttpMethod
import io.bandroid.simplefetch.SimpleJson
import io.bandroid.simplefetch.path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SimpleFetch", appName)
    }

    @Test
    fun `test library integration in app module`() {
        val json = """{"project": "SimpleFetch", "author": {"name": "Developer"}}"""
        val map = SimpleJson.parseObject(json)
        assertEquals("SimpleFetch", map["project"])
        assertEquals("Developer", map.path("author.name"))

        val req = FetchRequest(
            url = "https://api.example.com/items",
            method = HttpMethod.POST,
            body = map
        )
        assertEquals(HttpMethod.POST, req.method)
        assertNotNull(req.bodyAsString())
        assertTrue(req.bodyAsString()!!.contains("SimpleFetch"))
    }
}
