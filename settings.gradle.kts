rootProject.name = "sudoku"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

// Die Spiellogik (kern) ist reines Kotlin und baut überall.
// Die Android-App nur dort, wo ein Android-SDK liegt (GitHub Actions, Android Studio).
include(":kern")
val mitAndroid = System.getenv("ANDROID_HOME") != null ||
    System.getenv("ANDROID_SDK_ROOT") != null ||
    file("local.properties").exists()
if (mitAndroid) include(":app")
