// Top-level build file for wearing-drape (spec 000).
// Shared plugins and maintenance tasks only. No app code here.
// NOTE (AGP 9): Kotlin support is built into AGP ("built-in Kotlin"), so the
// org.jetbrains.kotlin.android plugin MUST NOT be applied. Only the Compose
// compiler plugin is declared (version matches AGP's embedded Kotlin).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
