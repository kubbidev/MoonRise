import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.io.ByteArrayOutputStream

plugins {
    java
}

base {
    archivesName.set("moonrise")
}

fun determinePatchVersion(): Int {
    // get the name of the last tag
    val tagInfo = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = tagInfo
    }
    val tagString = String(tagInfo.toByteArray())
    if (tagString.contains("-")) {
        return tagString.split("-")[1].toInt()
    }
    return 0
}

val majorVersion = "1"
val minorVersion = "0"
val patchVersion = determinePatchVersion()
val releaseVersion = "$majorVersion.$minorVersion"
val projectVersion = "$releaseVersion.$patchVersion"

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "me.kubbidev"
    version = "1.0-SNAPSHOT"

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        withSourcesJar()
    }

    extra["releaseVersion"] = releaseVersion
    extra["projectVersion"] = projectVersion

    repositories {
        mavenCentral()
    }

    dependencies {
        // test
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.4")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
        testImplementation("org.testcontainers:junit-jupiter:1.20.4")
        testImplementation("org.mockito:mockito-core:5.14.2")
        testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<Test>().configureEach {
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        }
    }
}