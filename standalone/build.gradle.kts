plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":common"))
    compileOnly(project(":common:loader-utils"))
    compileOnly(project(":standalone:app"))

    compileOnly("org.spongepowered:configurate-yaml:3.7.2")
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