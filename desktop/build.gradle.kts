plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.6.0"
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "FarmSyncCompanion"
        }
    }
}
