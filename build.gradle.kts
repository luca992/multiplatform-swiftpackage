import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.com.vanniktech.maven.publish)
}

version = "2.3.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.gradle.plugin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
            id = "io.github.luca992.multiplatform-swiftpackage"
            implementationClass = "com.chromaticnoise.multiplatformswiftpackage.MultiplatformSwiftPackagePlugin"
        }
    }
}


afterEvaluate {
    publishing {
        publications {
            named<MavenPublication>("pluginMaven") {
                groupId = "io.github.luca992.multiplatform-swiftpackage"
                artifactId = "io.github.luca992.multiplatform-swiftpackage.gradle.plugin"
            }
        }
    }
}

mavenPublishing {
    pom {
        name.set("Multiplatform Swift Package")
        description.set("Gradle plugin to generate a Swift.package file and XCFramework to distribute a Kotlin Multiplatform iOS library")
        url.set("https://github.com/luca992/multiplatform-swiftpackage")

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
            developer {
                name.set("Luca Spinazzola")
            }
        }
        scm {
            connection.set("scm:git:https://github.com/luca992/multiplatform-swiftpackage.git")
            developerConnection.set("scm:git:ssh://github.com/luca992/multiplatform-swiftpackage.git")
            url.set("https://github.com/luca992/multiplatform-swiftpackage")
        }
    }
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

allprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_1_8
    }
}