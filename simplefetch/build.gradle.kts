plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

kotlin {
    android {
        namespace = "io.bandroid.simplefetch"
        compileSdk = 36
        minSdk = 24
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.compose.runtime)
        }
        commonTest.dependencies {
            implementation(libs.junit)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}

group = "io.bandroid.simplefetch"
version = "1.0.0"

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/" + (System.getenv("GITHUB_REPOSITORY") ?: "bandroid/SimpleFetch"))
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: System.getenv("USERNAME")
                password = System.getenv("GITHUB_TOKEN") ?: System.getenv("TOKEN")
            }
        }
    }
}

