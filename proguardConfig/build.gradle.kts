plugins {
    kotlin("jvm")
    id("maven-publish")
}
repositories {
    google()
    mavenCentral()
}



dependencies {
    println(gradleApi().group)
    api(gradleApi())
    implementation("com.android.tools.build:gradle:8.5.0")
}

kotlin {
    jvmToolchain(11)
}


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("java") {
                groupId = "io.github.cloak-box.moshi"
                artifactId = "plugin"
                version = "1.0.0.1-SNAPSHOT"
                from(components["java"])
            }
        }
    }
}
