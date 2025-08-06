package com.chromaticnoise.multiplatformswiftpackage.task

import com.chromaticnoise.multiplatformswiftpackage.domain.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import java.io.File


internal fun getMacosFrameworks(configuration: PluginConfiguration): List<AppleFramework> {
    return configuration.appleTargets.mapNotNull { it.getFramework(configuration.buildConfiguration) }
        .filter {
            it.linkTask.name.contains("MacosX64") || it.linkTask.name.contains("MacosArm64")
        }
}

internal fun getIosSimulatorFrameworks(configuration: PluginConfiguration): List<AppleFramework> {
    return configuration.appleTargets.mapNotNull { it.getFramework(configuration.buildConfiguration) }
        .filter {
            it.linkTask.name.contains("IosX64") || it.linkTask.name.contains("IosSimulatorArm64")
        }
}

internal fun getWatchosSimulatorFrameworks(configuration: PluginConfiguration): List<AppleFramework> {
    return configuration.appleTargets.mapNotNull { it.getFramework(configuration.buildConfiguration) }
        .filter {
            it.linkTask.name.contains("WatchosX86")
                    || it.linkTask.name.contains("WatchosX64")
                    || it.linkTask.name.contains("WatchosDeviceArm64")
                    || it.linkTask.name.contains("WatchosSimulatorArm64")
        }
}

internal fun getTvosSimulatorFrameworks(configuration: PluginConfiguration): List<AppleFramework> {
    return configuration.appleTargets.mapNotNull { it.getFramework(configuration.buildConfiguration) }
        .filter {
            it.linkTask.name.contains("TvosX64") || it.linkTask.name.contains("TvosSimulatorArm64")
        }
}

internal fun Project.registerCreateUniversalMacosFrameworkTask() =
    tasks.register<FatFrameworkTask>("createUniversalMacosFramework") {
        group = "multiplatform-swift-package"
        description = "Creates a universal (fat) macos framework"
        val configuration = getConfigurationOrThrow()
        onlyIf { getMacosFrameworks(configuration).size > 1 }
        val targets = getMacosFrameworks(configuration)
        dependsOn(targets.map { it.linkTask.name })
        if (targets.isNotEmpty()) {
            val buildType = if (targets[0].linkTask.name.contains("Release")) "release" else "debug"
            baseName = checkNotNull(targets.first().name.value)
            destinationDirProperty.set(layout.buildDirectory.dir("bin/macosUniversal/${buildType}Framework"))
            from(targets.mapNotNull { it.framework })
        }
    }

internal fun Project.registerCreateUniversalIosSimulatorFrameworkTask() =
    tasks.register<FatFrameworkTask>("createUniversalIosSimulatorFramework") {
        group = "multiplatform-swift-package"
        description = "Creates a universal (fat) ios simulator framework"
        val configuration = getConfigurationOrThrow()
        onlyIf { getIosSimulatorFrameworks(configuration).size > 1 }
        val targets = getIosSimulatorFrameworks(configuration)
        dependsOn(targets.map { it.linkTask.name })
        if (targets.isNotEmpty()) {
            val buildType = if (targets[0].linkTask.name.contains("Release")) "release" else "debug"
            baseName = checkNotNull(targets.first().name.value)
            destinationDirProperty.set(layout.buildDirectory.dir("bin/iosSimulatorUniversal/${buildType}Framework"))
            from(targets.mapNotNull { it.framework })
        }
    }

internal fun Project.registerCreateUniversalWatchosSimulatorFrameworkTask() =
    tasks.register("createUniversalWatchosSimulatorFramework", FatFrameworkTask::class.java) {
        group = "multiplatform-swift-package"
        description = "Creates a universal (fat) watchos simulator framework"
        val configuration = getConfigurationOrThrow()
        onlyIf { getWatchosSimulatorFrameworks(configuration).size > 1 }
        val targets = getWatchosSimulatorFrameworks(configuration)
        dependsOn(targets.map { it.linkTask.name })
        if (targets.isNotEmpty()) {
            val buildType = if (targets[0].linkTask.name.contains("Release")) "release" else "debug"
            baseName = checkNotNull(targets.first().name.value)
            destinationDirProperty.set(layout.buildDirectory.dir("bin/watchosSimulatorUniversal/${buildType}Framework"))
            from(targets.mapNotNull { it.framework })
        }
    }

internal fun Project.registerCreateUniversalTvosSimulatorFrameworkTask() =
    tasks.register("createUniversalTvosSimulatorFramework", FatFrameworkTask::class.java) {
        group = "multiplatform-swift-package"
        description = "Creates a universal (fat) tvos simulator framework"
        val configuration = getConfigurationOrThrow()
        onlyIf { getTvosSimulatorFrameworks(configuration).size > 1 }
        val targets = getTvosSimulatorFrameworks(configuration)
        dependsOn(targets.map { it.linkTask.name })
        if (targets.isNotEmpty()) {
            val buildType = if (targets[0].linkTask.name.contains("Release")) "release" else "debug"
            baseName = checkNotNull(targets.first().name.value)
            destinationDirProperty.set(layout.buildDirectory.dir("bin/tvosSimulatorUniversal/${buildType}Framework"))
            from(targets.mapNotNull { it.framework })
        }
    }


internal fun removeMonoFrameworksAndAddUniversalFrameworkIfNeeded(
    binFolderPrefix: String,
    buildDir: File,
    monoFrameworks: List<AppleFramework>,
    outputFrameworks: MutableList<AppleFramework>
) {
    if (monoFrameworks.size > 1) {
        monoFrameworks.forEach { mono ->
            outputFrameworks.removeIf { mono.outputFile == it.outputFile }
        }
        val frameworkName = monoFrameworks[0].name
        val frameworkNameLegalChars = frameworkName.value.replace("-", "_")
        val buildType = if (monoFrameworks[0].linkTask.name.contains("Release")) "release" else "debug"
        val destinationDir = buildDir.resolve("bin/${binFolderPrefix}Universal/${buildType}Framework")
        val outputFile = AppleFrameworkOutputFile(File(destinationDir, "${frameworkNameLegalChars}.framework"))
        outputFrameworks.add(
            AppleFramework(
                outputFile,
                frameworkName,
                AppleFrameworkLinkTask("")
            )
        )
    }
}


internal fun Project.registerCreateXCFrameworkTask() = tasks.register("createXCFramework") {
    group = "multiplatform-swift-package"
    description = "Creates an XCFramework for all declared Apple targets"

    val configuration = getConfigurationOrThrow()
    val xcFrameworkDestination =
        File(configuration.outputDirectory.value, "${configuration.packageName.value}.xcframework")
    val outputFrameworks =
        configuration.appleTargets.mapNotNull { it.getFramework(configuration.buildConfiguration) }.toMutableList()

    dependsOn(outputFrameworks.map { it.linkTask.name })
    dependsOn("createUniversalMacosFramework")
    dependsOn("createUniversalIosSimulatorFramework")
    dependsOn("createUniversalWatchosSimulatorFramework")
    dependsOn("createUniversalTvosSimulatorFramework")

    val macosFrameworks = getMacosFrameworks(configuration)
    removeMonoFrameworksAndAddUniversalFrameworkIfNeeded(
        "macos",
        layout.buildDirectory.asFile.get(),
        macosFrameworks,
        outputFrameworks
    )
    val iosSimulatorFrameworks = getIosSimulatorFrameworks(configuration)
    removeMonoFrameworksAndAddUniversalFrameworkIfNeeded(
        "iosSimulator",
        layout.buildDirectory.asFile.get(),
        iosSimulatorFrameworks,
        outputFrameworks
    )
    val watchosSimulatorFrameworks = getWatchosSimulatorFrameworks(configuration)
    removeMonoFrameworksAndAddUniversalFrameworkIfNeeded(
        "watchosSimulator",
        layout.buildDirectory.asFile.get(),
        watchosSimulatorFrameworks,
        outputFrameworks
    )
    val tvosSimulatorFrameworks = getTvosSimulatorFrameworks(configuration)
    removeMonoFrameworksAndAddUniversalFrameworkIfNeeded(
        "tvosSimulator",
        layout.buildDirectory.asFile.get(),
        tvosSimulatorFrameworks,
        outputFrameworks
    )

    doLast {
        val processBuilder = ProcessBuilder().apply {
            command(mutableListOf<String>().apply {
                add("xcodebuild")
                add("-create-xcframework")
                add("-output")
                add(xcFrameworkDestination.path)
                outputFrameworks.forEach { framework ->
                    add("-framework")
                    add(framework.outputFile.path)

                    framework.dsymFile.takeIf { it.exists() }?.let { dsymFile ->
                        add("-debug-symbols")
                        add(dsymFile.absolutePath)
                    }
                }
            })
        }

        val process = processBuilder.start()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException("xcodebuild command failed with exit code: $exitCode")
        }
    }

    doFirst {
        xcFrameworkDestination.deleteRecursively()
    }
}
