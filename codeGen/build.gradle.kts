plugins {
    kotlin("jvm") version "1.9.23"
//    id("com.black.cat.plugin.moshiExtPlugin")
}


repositories{
    mavenLocal()
    mavenCentral()
    google()
}
dependencies {
    implementation(libs.com.squareup.kotlinpoet.ksp)
    compileOnly(libs.com.google.devtools.ksp.symbol.processing.api)
    compileOnly(libs.com.google.devtools.ksp.symbol.processing.gradle.plugin)
    compileOnly(libs.com.google.devtools.ksp.symbol.processing)
    implementation(libs.com.squareup.moshi)
}


kotlin {
    jvmToolchain(11)
}