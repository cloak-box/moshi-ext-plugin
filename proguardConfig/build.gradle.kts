plugins {
    kotlin("jvm")
    id("com.black.cat.plugin.JavaApiPublishPlugin")
}
repositories {
    google()
    mavenCentral()
}



dependencies {
    api(gradleApi())
    implementation("com.android.tools.build:gradle:8.5.0")
}

kotlin {
    jvmToolchain(11)
}


mavenPublishing {
    mavenConfig {
        groupId = "io.github.cloak-box.plugin"
        artifactId = "proguardConfig"
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