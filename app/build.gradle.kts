plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "io.github.jsusoh.sudoku"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.jsusoh.sudoku"
        minSdk = 26
        targetSdk = 35
        // Jeder Lauf von GitHub Actions zählt hoch, damit sich neue Versionen drüber installieren lassen.
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
        versionName = "1.0.${versionCode}"
    }

    signingConfigs {
        // Ein fester Schlüssel im Repo: Jede neue APK lässt sich über die alte installieren,
        // ohne dass der Spielstand verloren geht. Für den Play Store bräuchte es einen geheimen Schlüssel.
        create("familie") {
            storeFile = file("sudoku.keystore")
            storePassword = "sudoku-mama"
            keyAlias = "sudoku"
            keyPassword = "sudoku-mama"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("familie")
        }
        debug {
            signingConfig = signingConfigs.getByName("familie")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":kern"))

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
}
