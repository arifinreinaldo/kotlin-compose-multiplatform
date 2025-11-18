plugins {
    // Kotlin Multiplatform
    kotlin("multiplatform") version "1.9.21" apply false
    kotlin("android") version "1.9.21" apply false

    // Android
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false

    // Compose Multiplatform
    id("org.jetbrains.compose") version "1.5.11" apply false

    // SQLDelight
    id("app.cash.sqldelight") version "2.0.1" apply false
}

allprojects {
    group = "com.example.kmpcleanarch"
    version = "1.0.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
