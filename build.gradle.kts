import com.palantir.gradle.docker.DockerExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

plugins {
    kotlin(module = "jvm") version "1.2.21"
}

buildscript {
    val kotlinVersion = "1.2.21"
    val springBootVersion = "2.0.0.RC1"
    val gradleDockerVersion = "0.17.2"

    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://repo.spring.io/snapshot") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/libs-milestone") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${kotlinVersion}")
        classpath("gradle.plugin.com.palantir.gradle.docker:gradle-docker:${gradleDockerVersion}")
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.3")
        classpath("gradle.plugin.com.avast.gradle:gradle-docker-compose-plugin:0.6.17")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:3.10.0")
    }
}

apply {
    plugin("kotlin-spring")
    plugin("eclipse")
    plugin("org.springframework.boot")
    plugin("io.spring.dependency-management")
    plugin("com.palantir.docker")
    plugin("org.junit.platform.gradle.plugin")
    plugin("com.avast.gradle.docker-compose")
    plugin("com.diffplug.gradle.spotless")
}



group = "com.disney.studios"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

spotless {
    freshmark {
        target(files("README.md"))
        //propertiesFile('gradle.properties')
    }

    kotlin {
        paddedCell()
        ktlint()
    }

    format ("misc") {
        target(files("**/*.gradle", "**/*.md", "**/.gitignore"))
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
        paddedCell()
    }
}

val jar: Jar by tasks
docker {
    name = "${project.group}/${jar.baseName}"
    files(jar.archivePath)
    buildArgs(mapOf("JAR_FILE" to jar.archiveName))
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://repo.spring.io/milestone") }
}


dependencies {
    compile("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    compile("org.springframework.boot:spring-boot-starter-webflux")
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile(kotlin("stdlib"))
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.0")
    testCompile("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit", module = "junit")
    }
    testCompile("io.projectreactor:reactor-test")
    testCompile("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
    testCompile("org.junit.jupiter:junit-jupiter-api")
    testRuntime("org.junit.jupiter:junit-jupiter-engine")
    testRuntime("org.junit.platform:junit-platform-engine")
    testCompile("com.nhaarman:mockito-kotlin:1.5.0")
}


/**
 * Configures the [docker][DockerExtension] project extension.
 */
val Project.docker get() = extensions.getByName("docker") as DockerExtension
fun Project.docker(configure: DockerExtension.() -> Unit): Unit = extensions.configure("docker", configure)

/**
 * Configures the [docker][SpotlessExtension] project extension.
 */
val Project.spotless get() = extensions.getByName("spotless") as SpotlessExtension
fun Project.spotless(configure: SpotlessExtension.() -> Unit): Unit = extensions.configure("spotless", configure)
