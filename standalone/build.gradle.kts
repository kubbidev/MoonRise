plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":common"))
    compileOnly(project(":common:loader-utils"))
    compileOnly(project(":standalone:app"))

    compileOnly("org.spongepowered:configurate-yaml:3.7.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    testImplementation("org.testcontainers:junit-jupiter:1.18.3")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")

    testImplementation("com.zaxxer:HikariCP:4.0.3")
    testImplementation("org.postgresql:postgresql:42.6.0")
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
    testImplementation("mysql:mysql-connector-java:8.0.23")
    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.1.3")
    testImplementation("org.spongepowered:configurate-hocon:3.7.2")
    testImplementation("org.yaml:snakeyaml:1.28")
    
    testImplementation(project(":standalone:app"))
    testImplementation(project(":common:loader-utils"))
}

tasks.shadowJar {
    archiveFileName = "moonrise-standalone.jarinjar"

    dependencies {
        include(dependency("me.kubbidev.moonrise:.*"))
    }

    relocate("net.kyori.event", "me.kubbidev.moonrise.lib.eventbus")
    relocate("com.github.benmanes.caffeine", "me.kubbidev.moonrise.lib.caffeine")
    relocate("okio", "me.kubbidev.moonrise.lib.okio")
    relocate("okhttp3", "me.kubbidev.moonrise.lib.okhttp3")
    relocate("org.mariadb.jdbc", "me.kubbidev.moonrise.lib.mariadb")
    relocate("com.mysql", "me.kubbidev.moonrise.lib.mysql")
    relocate("org.postgresql", "me.kubbidev.moonrise.lib.postgresql")
    relocate("com.zaxxer.hikari", "me.kubbidev.moonrise.lib.hikari")
    relocate("ninja.leaping.configurate", "me.kubbidev.moonrise.lib.configurate")
    relocate("org.yaml.snakeyaml", "me.kubbidev.moonrise.lib.yaml")
    relocate("net.dv8tion.jda", "me.kubbidev.moonrise.lib.jda")
}

artifacts {
    archives(tasks.shadowJar)
}