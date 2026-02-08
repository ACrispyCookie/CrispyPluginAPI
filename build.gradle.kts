plugins {
    `java-library`
    `maven-publish`
}

allprojects {
    group = "dev.acrispycookie"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        mavenLocal() // Important for finding the legacy Spigot jars
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}

tasks.clean {
    dependsOn(subprojects.map { it.tasks.named("clean") })
}

tasks.named("publishToMavenLocal") {
    dependsOn(subprojects.map { it.tasks.named("publishToMavenLocal") })
}