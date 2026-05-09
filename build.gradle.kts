// Root build file for Still.
// Plugin versions live here so the app module can stay small and readable.
// AGP 9.0+ ships with built-in Kotlin support, so the kotlin.android plugin is no longer applied.
plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
}
