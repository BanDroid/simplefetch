package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CodeSnippetCard

@Composable
fun PublishGuideScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Publish,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Module & Publishing Guide",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Instructions to publish SimpleFetch and use it across any project",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Architecture Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Project Module Architecture", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "This repository is structured cleanly into two modules:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(":simplefetch (Kotlin Multiplatform Library)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Multiplatform targets: Android & Desktop/JVM. Common code in commonMain with pluggable HttpEngine, zero-dependency SimpleJson, and Compose Multiplatform fetchAsState.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text(":app (Sample Showcase Module)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "Interactive REST Studio and Compose UI testbed demonstrating real HTTP calls and unit tests.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // How to Publish Card
        CodeSnippetCard(
            title = "Step 1: Publish to Maven Local or Remote",
            code = """
# Build and publish to local ~/.m2 repository for testing:
gradle :simplefetch:publishReleasePublicationToMavenLocal

# Publish to Maven Central / Sonatype / Nexus repository:
gradle :simplefetch:publish
""".trimIndent()
        )

        // How to consume in other projects
        CodeSnippetCard(
            title = "Step 2: Add to Any Compose Multiplatform Project",
            code = """
// settings.gradle.kts (add mavenLocal or maven repo):
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal() // for locally published builds
    }
}

// build.gradle.kts (or commonMain in KMP):
dependencies {
    implementation("io.bandroid.simplefetch:simplefetch:1.0.0")
}
""".trimIndent()
        )

        // Testing the library efficiently
        CodeSnippetCard(
            title = "Step 3: Unit Testing SimpleFetch Calls",
            code = """
@Test
fun testSimpleFetchMapAccess() = runTest {
    // 1. Direct coroutine call
    val result = SimpleFetch.get("https://jsonplaceholder.typicode.com/posts/1")

    assertTrue(result.isSuccess)
    assertEquals(200, result.statusCode)

    // 2. Untyped map verification
    val map = result.jsonMap
    assertNotNull(map["title"])
    assertEquals(1L, map["id"])
}
""".trimIndent()
        )
    }
}
