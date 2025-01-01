plugins {
    `maven-publish`
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.1")
}

// Only used occasionally for deployment - not needed for normal builds.
publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "api"
            version = project.extra["releaseVersion"].toString()

            from(components["java"])
            pom {
                name = "MoonRise"
                description = "A simple but powerful Discord Bot used to enhance your Discord experience."
                url = "https://gitlab.com/kubbidev/moonrise"

                licenses {
                    license {
                        name = "CC BY-NC-SA 4.0"
                        url = "https://creativecommons.org/licenses/by-nc-sa/4.0/"
                    }
                }

                developers {
                    developer {
                        id = "kubbidev"
                        name = "kubbi"
                        url = "https://kubbidev.me"
                    }
                }

                issueManagement {
                    system = "Gitlab"
                    url = "https://gitlab.com/kubbidev/moonrise/-/issues"
                }
            }
        }
    }
    repositories {
        maven(url = "https://nexus.kubbidev.me/repository/maven-releases/") {
            name = "kubbidev-releases"
            credentials(PasswordCredentials::class) {
                username = System.getenv("GRADLE_KUBBIDEV_RELEASES_USER")
                    ?: property("kubbidev-releases-user") as String?

                password = System.getenv("GRADLE_KUBBIDEV_RELEASES_PASS")
                    ?: property("kubbidev-releases-pass") as String?
            }
        }
    }
}