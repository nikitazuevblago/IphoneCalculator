// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    //id("org.jetbrains.compose") version "1.5.0" apply false
    alias(libs.plugins.compose.compiler) apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.5.0")
    }
}