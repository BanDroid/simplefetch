# SimpleFetch 🚀

A lightweight, zero-abstraction REST data fetching library built natively for **Compose Multiplatform** and Kotlin applications.

SimpleFetch makes API calls as effortless as possible:
* **True Kotlin & Compose Multiplatform**: Code resides in `commonMain`, with preconfigured targets for **Android** and **Desktop/JVM**, ready to integrate into any Compose Multiplatform app.
* **Single-Block Calling**: Write a single line of code to initiate and observe network request states directly.
* **No Schema Typing Needed**: Uses flexible Kotlin `Map<String, Any?>` and `List<Any?>` to handle arbitrary JSON structures without declaring boilerplate data classes or serializers.
* **Full REST Support**: Direct methods for `GET`, `POST`, `PUT`, `DELETE`, and `PATCH`.
* **Zero Heavy Dependencies**: Uses a pure Kotlin, reflection-free JSON parser and standard HTTP engine without third-party network bloat.
* **Pluggable Multiplatform Engines**: Includes a built-in `DefaultHttpEngine` for immediate zero-config use on Android/JVM, with support for custom engines (`HttpEngine`) like Ktor, Darwin, or Mock engines for tests.
* **Dual Output Support**: Works seamlessly with JSON objects, arrays, and plain text responses.

---

## 📦 Package & Dependency

* **Package:** `io.bandroid.simplefetch`
* **Artifact:** `io.bandroid.simplefetch:simplefetch:1.0.0`

### 1. Gradle Setup

In your Compose Multiplatform project (`build.gradle.kts`):

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal() // for locally published builds
    }
}

// build.gradle.kts (commonMain in Compose Multiplatform or standard app dependencies):
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.bandroid.simplefetch:simplefetch:1.0.0")
        }
    }
}
```

---

## ⚡ Quick Start

### 1. Single-Block Declarative UI in Compose (`fetchAsState`)

Initiate and reactively observe requests in a single line of code:

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import io.bandroid.simplefetch.fetchAsState
import io.bandroid.simplefetch.FetchState
import io.bandroid.simplefetch.getString

@Composable
fun UserProfile(userId: Int) {
    // Single block: Initiates and observes state automatically on key change
    val state by fetchAsState(
        url = "https://jsonplaceholder.typicode.com/users/$userId",
        key = userId
    )

    when (val s = state) {
        is FetchState.Loading -> CircularProgressIndicator()
        is FetchState.Success -> {
            val user = s.result.jsonMap
            Text("Name: ${user.getString("name")}")
            Text("Email: ${user.getString("email")}")
        }
        is FetchState.Error -> Text("Failed: ${s.message}")
        else -> {}
    }
}
```

---

### 2. Interactive Events & Forms (`rememberFetchController`)

Trigger `POST`, `PUT`, or `DELETE` requests with dynamic Map payloads on user clicks:

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import io.bandroid.simplefetch.rememberFetchController
import io.bandroid.simplefetch.FetchState

@Composable
fun CreatePostButton() {
    val controller = rememberFetchController()

    Button(onClick = {
        // Send a dynamic map directly — no data class needed!
        controller.post(
            url = "https://jsonplaceholder.typicode.com/posts",
            body = mapOf(
                "title" to "My New Post",
                "body" to "Hello Compose Multiplatform!",
                "userId" to 1
            )
        )
    }) {
        Text("Submit Post")
    }

    when (val s = controller.state.value) {
        is FetchState.Loading -> Text("Sending...")
        is FetchState.Success -> Text("Created Post ID: ${s.result.jsonMap["id"]}")
        is FetchState.Error -> Text("Error: ${s.message}")
        else -> {}
    }
}
```

---

### 3. Direct Coroutine Calls (Suspend Functions)

For repositories, view models, or background workers:

```kotlin
import io.bandroid.simplefetch.SimpleFetch
import io.bandroid.simplefetch.HttpMethod
import io.bandroid.simplefetch.path

suspend fun loadData() {
    // 1. GET Request
    val getResult = SimpleFetch.get("https://jsonplaceholder.typicode.com/posts/1")
    if (getResult.isSuccess) {
        val map = getResult.jsonMap
        println("Title: ${map["title"]}")
    }

    // 2. POST with Map Payload
    val postResult = SimpleFetch.post(
        url = "https://jsonplaceholder.typicode.com/posts",
        body = mapOf("title" to "Launch", "userId" to 1)
    )
    println("HTTP Status: ${postResult.statusCode}")

    // 3. PUT Request
    SimpleFetch.put(
        url = "https://jsonplaceholder.typicode.com/posts/1",
        body = mapOf("title" to "Updated Title")
    )

    // 4. DELETE Request
    val deleteResult = SimpleFetch.delete("https://jsonplaceholder.typicode.com/posts/1")
    println("Deleted: ${deleteResult.isSuccess}")

    // 5. DSL Builder
    val customResult = SimpleFetch.fetch {
        url = "https://api.example.com/search"
        method = HttpMethod.GET
        query("query", "compose")
        query("limit", 10)
        header("Authorization", "Bearer my-token")
    }
}
```

---

### 4. Dynamic Untyped Map & Path Navigation

Extract nested fields using dot notation without null-pointer hazards:

```kotlin
// Example JSON: { "user": { "address": { "geo": { "lat": "-37.3159" } } } }
val lat = result.jsonMap.path("user.address.geo.lat")

// Typed extractors with default fallbacks:
val count: Int = result.jsonMap.getInt("count", default = 0)
val flag: Boolean = result.jsonMap.getBoolean("active", default = false)
val name: String = result.jsonMap.getString("name")
```

---

### 5. Multiplatform HTTP Engines & Custom Interceptors

SimpleFetch defines a clean `HttpEngine` interface:

```kotlin
interface HttpEngine {
    suspend fun execute(request: FetchRequest, config: SimpleFetchConfig): FetchResult
}

// Out-of-the-box standard engine:
SimpleFetch.engine = DefaultHttpEngine()

// Plug in a custom engine (e.g. Mock engine for testing or Ktor):
class MockHttpEngine : HttpEngine {
    override suspend fun execute(request: FetchRequest, config: SimpleFetchConfig): FetchResult {
        return FetchResult(
            statusCode = 200,
            text = """{"id": 99, "status": "mocked"}"""
        )
    }
}
SimpleFetch.engine = MockHttpEngine()
```

---

## 🛠️ Multiplatform Module Architecture

```
.
├── simplefetch/       # Kotlin Multiplatform library module (io.bandroid.simplefetch)
│   ├── build.gradle.kts (Kotlin Multiplatform + com.android.kotlin.multiplatform.library)
│   └── src/
│       ├── commonMain/kotlin/io/bandroid/simplefetch/
│       │   ├── SimpleFetch.kt      # Core HTTP engine, HttpEngine interface & suspend functions
│       │   ├── FetchCompose.kt     # fetchAsState & rememberFetchController
│       │   ├── SimpleJson.kt       # Pure Kotlin zero-dependency Map/List parser & extensions
│       │   ├── FetchRequest.kt     # Pure Kotlin URL component encoder & request model
│       │   ├── FetchResult.kt      # Response container (status, headers, text, jsonMap)
│       │   ├── FetchState.kt       # Reactive UI state (Idle, Loading, Success, Error)
│       │   └── HttpMethod.kt       # GET, POST, PUT, DELETE, PATCH, HEAD
│       └── commonTest/kotlin/io/bandroid/simplefetch/
│           ├── FetchRequestTest.kt # URL encoding and request tests
│           └── SimpleJsonTest.kt   # JSON parsing, maps, arrays, and path tests
└── app/               # Showcase sample module
    └── src/main/java/com/example/ui/screens/
        ├── RestStudioScreen.kt   # Interactive REST tester & Code generator
        ├── ComposeDemosScreen.kt # Live fetchAsState & Form demos
        └── PublishGuideScreen.kt # In-app documentation
```

### Publishing Commands

* **Publish to Local Maven (`~/.m2`):**
  ```bash
  gradle :simplefetch:publishReleasePublicationToMavenLocal
  ```

* **Publish to Remote Maven Repository:**
  ```bash
  gradle :simplefetch:publish
  ```

* **Run Unit Tests:**
  ```bash
  gradle test
  ```

---

## 📄 License
MIT License. Free for open source and commercial use.
