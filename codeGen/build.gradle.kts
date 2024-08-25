plugins {
    kotlin("jvm") version "1.9.23"
    id("com.black.cat.plugin.JavaApiPublishPlugin")
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
mavenPublishing {
    mavenConfig {
        groupId = "io.github.cloak-box.ksp"
        artifactId = "moshi-codeGen"
        version = libs.versions.publish.lib.version.get()
        publishJavadocJar = false
        poublicSourcesJar = false
        mavenRepo = "java"
        mavenCentralUsername = properties["mavenCentralUsername"].toString()
        mavenCentralPassword = properties["mavenCentralPassword"].toString()

        pom {
            name.set("cloak box")
            description.set("A description of what my library does.")
            inceptionYear.set("2020")
            url.set("https://github.com/cloak-box/Vbox")
            licenses {
                license {
                    name.set("GNU GENERAL PUBLIC LICENSE , Version 3, 29 June 2007")
                    url.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
                    distribution.set("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text")
                }
            }
            developers {
                developer {
                    id.set("cloak box")
                    name.set("cloak box")
                    url.set("https://github.com/cloak-box")
                }
            }
            scm {
                url.set("https://github.com/cloak-box/Vbox")
                connection.set("scm:git:git://github.com/cloak-box/Vbox.git")
                developerConnection.set("scm:git:ssh://git@github.com/cloak-box/Vbox.git")
            }
        }
    }
}