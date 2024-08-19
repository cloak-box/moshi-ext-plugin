plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
buildscript{
    dependencies{
        classpath("io.github.cloak-box.moshi:plugin:1.0.0.1-SNAPSHOT")
    }
    repositories{
        mavenLocal()
        mavenCentral()
        google()
    }
}

rootProject.name = "moshi-ext-plugin"

include(":codeGen")
include("proguardConfig")
