buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.13.1")
    }
}

apply(plugin = "binary-compatibility-validator")

group = "com.darek"

val GIT_USER: String? by project
val GIT_TOKEN: String? by project
val VERSION_NAME: String? by project

version = VERSION_NAME ?: "0.1"

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("gradle-plugin", "1.8.20"))
    testImplementation("io.kotest:kotest-runner-junit5:5.5.4")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    testImplementation("io.kotest:kotest-property:5.5.4")
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation(kotlin("gradle-plugin", "1.8.20"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

project.tasks.named("processResources", Copy::class.java) {
    // https://github.com/gradle/gradle/issues/17236
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

gradlePlugin {
    plugins {
        create("pluginMaven") {
            id = "com.dariusz.multiplatform-swiftpackage"
            implementationClass = "com.chromaticnoise.multiplatformswiftpackage.MultiplatformSwiftPackagePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            pom {
                groupId = "com.dariusz.multiplatform-swiftpackage"
                artifactId = "com.dariusz.multiplatform-swiftpackage.gradle.plugin"

                name.set("Multiplatform Swift Package")
                description.set("Gradle plugin to generate a Swift.package file and XCFramework to distribute a Kotlin Multiplatform iOS library")
                url.set("https://github.com/dariuszszlag/multiplatform-swiftpackage")

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Georg Dresler")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/dariuszszlag/multiplatform-swiftpackage")
            credentials {
                username = GIT_USER
                password = GIT_TOKEN
            }
        }
    }
}

signing {
    sign(publishing.publications["pluginMaven"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
