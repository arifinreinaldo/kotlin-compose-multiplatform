import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)

                // Koin for Desktop
                implementation("io.insert-koin:koin-core:3.5.3")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.kmpcleanarch.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KMP Clean Architecture"
            packageVersion = "1.0.0"
        }
    }
}
