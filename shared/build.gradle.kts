plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("app.cash.sqldelight")
    kotlin("plugin.serialization") version "1.9.21"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    jvm("desktop")

    // iOS targets disabled for Windows development
    // Uncomment when building on macOS
    /*
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    */

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // SQLDelight - Local Database (Single Source of Truth)
                implementation("app.cash.sqldelight:runtime:2.0.1")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")

                // Koin for Dependency Injection
                implementation("io.insert-koin:koin-core:3.5.3")
                implementation("io.insert-koin:koin-compose:1.1.2")

                // ViewModel
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

                // Ktor - Networking (Extension Point)
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("io.ktor:ktor-client-logging:2.3.7")

                // Kotlinx Serialization - JSON parsing
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

                // Voyager - Navigation (Extension Point)
                implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
                implementation("cafe.adriel.voyager:voyager-tab-navigator:1.0.0")
                implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")
                implementation("cafe.adriel.voyager:voyager-koin:1.0.0")

                // DateTime utilities
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
                implementation("io.insert-koin:koin-test:3.5.3")
                implementation("app.cash.turbine:turbine:1.0.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("app.cash.sqldelight:android-driver:2.0.1")
                implementation("io.ktor:ktor-client-okhttp:2.3.7")
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("io.mockk:mockk:1.13.8")
                implementation("org.robolectric:robolectric:4.11.1")
            }
        }

        // iOS source sets disabled for Windows development
        /*
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.0.1")
                implementation("io.ktor:ktor-client-darwin:2.3.7")
            }
        }
        */

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
                implementation("io.ktor:ktor-client-okhttp:2.3.7")
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("io.mockk:mockk:1.13.8")
            }
        }
    }
}

android {
    namespace = "com.example.kmpcleanarch"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.kmpcleanarch.database")
        }
    }
}
