import com.palantir.gradle.docker.DockerExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import org.apache.tools.ant.taskdefs.ExecTask
import org.gradle.api.internal.tasks.testing.junit.JUnitTestFramework
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.version

plugins {
    val kotlinVersion = "1.2.21"
    val springBootVersion = "2.0.0.RC1"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("com.diffplug.gradle.spotless") version "3.10.0"
    id("com.avast.gradle.docker-compose") version "0.7.1"
    id("com.palantir.docker") version "0.19.2"
    id("org.junit.platform.gradle.plugin") version "1.0.3"
    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.0.4.RELEASE"
    idea
    jacoco
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
        ktlint()
    }

    format("misc") {
        target(files("**/*.gradle.kts", "**/*.md", "**/.gitignore"))
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

tasks {

    withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    withType<JacocoReport> {
        group = "coverage"
//        executionData = files("$buildDir/jacoco/junitPlatformTest.exec")
        reports {
            xml.isEnabled = true
            xml.destination = file("$buildDir/reports/jacoco.xml")
            html.destination = file("$buildDir/jacocoHtml")
        }

        dependsOn("junitPlatformTest")
    }

    withType<JacocoCoverageVerification> {
        group = "coverage"
//        executionData = files("$buildDir/jacoco/junitPlatformTest.exec")
        violationRules {
            rule {
                element = "BUNDLE"
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = BigDecimal(0.78)
                }

                limit {
                    counter = "BRANCH"
                    value = "COVEREDRATIO"
                    minimum = BigDecimal(0.56)
                }

                limit {
                    counter = "LINE"
                    value = "COVEREDRATIO"
                    minimum = BigDecimal(0.83)
                }
            }
        }
    }

    val junitPlatformTest: JavaExec by tasks
    withType<Test> {
        jacoco {
            applyTo(junitPlatformTest)
        }
    }

    junitPlatformTest.apply {
        val jacoco : JacocoTaskExtension by extensions
        jacoco.apply {
            destinationFile = file("${buildDir}/jacoco/test.exec")
        }
    }

    "openCoverage" {
        group = "coverage"
        doLast {
            exec {
                commandLine("open", "$buildDir/jacocoHtml/index.html")
            }
        }
    }
}

val jar: Jar by tasks

docker {
    name = "${project.group}/${jar.baseName}"
    files(jar.archivePath)
    buildArgs(mapOf("JAR_FILE" to jar.archiveName))
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
