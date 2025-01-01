dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")

    api(project(":api"))
    api("org.jetbrains:annotations:26.0.1")

    compileOnly(project(":common:loader-utils"))

    compileOnly("org.slf4j:slf4j-api:1.7.30")
    compileOnly("org.apache.logging.log4j:log4j-api:2.14.0")

    api("net.kyori:adventure-api:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "checker-qual")
        exclude(module = "annotations")
    }

    api("net.kyori:adventure-text-serializer-gson:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
        exclude(module = "gson")
    }

    api("net.kyori:adventure-text-serializer-legacy:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-serializer-plain:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-minimessage:4.11.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:event-api:3.0.0") {
        exclude(module = "checker-qual")
        exclude(module = "guava")
    }

    api("com.google.code.gson:gson:2.7")
    api("com.google.guava:guava:19.0")

    api("com.github.ben-manes.caffeine:caffeine:2.9.0")
    api("com.squareup.okhttp3:okhttp:3.14.9")
    api("com.squareup.okio:okio:1.17.5")

    api("org.spongepowered:configurate-core:3.7.2") {
        isTransitive = false
    }
    api("org.spongepowered:configurate-yaml:3.7.2") {
        isTransitive = false
    }

    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("org.postgresql:postgresql:42.6.0")
    compileOnly("org.yaml:snakeyaml:1.28")
}