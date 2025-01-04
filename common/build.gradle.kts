dependencies {
    api(project(":api"))
    api("org.jetbrains:annotations:26.0.1")

    compileOnly(project(":common:loader-utils"))

    compileOnly("org.slf4j:slf4j-api:1.7.30")
    compileOnly("org.apache.logging.log4j:log4j-api:2.14.0")

    api("net.kyori:adventure-api:4.14.0") {
        exclude(module = "adventure-bom")
        exclude(module = "checker-qual")
        exclude(module = "annotations")
    }

    api("net.kyori:adventure-text-serializer-gson:4.14.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
        exclude(module = "gson")
    }

    api("net.kyori:adventure-text-serializer-legacy:4.14.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-serializer-plain:4.14.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:adventure-text-minimessage:4.14.0") {
        exclude(module = "adventure-bom")
        exclude(module = "adventure-api")
    }

    api("net.kyori:event-api:3.0.0") {
        exclude(module = "checker-qual")
        exclude(module = "guava")
    }

    api("com.google.code.gson:gson:2.7")
    api("com.google.guava:guava:19.0")

    api("com.github.ben-manes.caffeine:caffeine:2.9.0") {
        exclude(module = "error_prone_annotations")
        exclude(module = "checker-qual")
    }

    api("com.squareup.okhttp3:okhttp:3.14.9")
    api("com.squareup.okio:okio:1.17.5")

    api("org.spongepowered:configurate-core:3.7.2") {
        isTransitive = false
    }
    api("org.spongepowered:configurate-yaml:3.7.2") {
        isTransitive = false
    }

    api("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    api("com.fasterxml.jackson.core:jackson-core:2.17.2")
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("org.postgresql:postgresql:42.6.0")
    compileOnly("org.yaml:snakeyaml:1.28")

    // Discord dependencies: https://github.com/discord-jda/JDA
    compileOnly("net.dv8tion:JDA:5.2.2") { exclude(module = "opus-java") }
}