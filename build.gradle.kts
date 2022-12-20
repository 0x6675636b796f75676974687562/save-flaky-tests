import org.gradle.api.file.DuplicatesStrategy.EXCLUDE
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.22"
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.2.3"
    eclipse
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "0x6675636b796f75676974687562/save-flaky-tests"
        url = uri("https://maven.pkg.github.com/0x6675636b796f75676974687562/save-flaky-tests")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

internal val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

internal val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

internal val build: Task by tasks
build.dependsOn("fatJar")

tasks.register<Jar>("fatJar") {
    group = "Build"
    description = "Assembles a jar archive containing the main classes as well as dependencies."
    dependsOn("classes")

    manifest {
        attributes["Main-Class"] = "com.saveourtool.save.test.generator.Main"
    }
    archiveClassifier.set("all")
    duplicatesStrategy = EXCLUDE

    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { entry ->
        when {
            entry.isDirectory -> entry
            else -> zipTree(entry)
        }
    })
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/0x6675636b796f75676974687562/save-flaky-tests")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            groupId = "com.saveourtool.save"
            version = "0.0.1-SNAPSHOT"

            from(components["java"])
            artifact(tasks["fatJar"])
        }
    }
}
