import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("signing")
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation("com.android.tools.build:gradle:8.12.1")
    implementation("com.akuleshov7:ktoml-core:0.7.1")
    implementation("com.akuleshov7:ktoml-file:0.7.1")
}
gradlePlugin {
    plugins {
        create("cargo-ndk-plugin") {
            id = "io.github.thatworld.cargondk"
            implementationClass = "io.github.thatworld.cargondk.plugin.CargoNDKPlugin"
        }
    }
}

mavenPublishing {
    coordinates("io.github.thatworld", "cargondk", "0.0.3")

    pom {
        name.set("cargo-ndk-plugin")
        description.set("A Gradle plugin for compiling native code using cargo-ndk.")
        url.set("https://github.com/thatworld/cargo-ndk-plugin")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                name.set("Gang")
                url.set("https://github.com/thatworld/cargo-ndk-plugin")
            }
        }
        scm {
            url.set("https://github.com/thatworld/cargo-ndk-plugin")
            connection.set("scm:git:git://github.com/thatworld/cargo-ndk-plugin.git")
            developerConnection.set("scm:git:ssh://git@github.com/thatworld/cargo-ndk-plugin.git")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}