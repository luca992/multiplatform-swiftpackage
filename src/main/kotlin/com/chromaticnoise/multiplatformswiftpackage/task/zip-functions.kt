package com.chromaticnoise.multiplatformswiftpackage.task

import com.chromaticnoise.multiplatformswiftpackage.domain.OutputDirectory
import com.chromaticnoise.multiplatformswiftpackage.domain.ZipFileName
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File

internal fun zipFileChecksum(project: Project, outputDirectory: OutputDirectory, zipFileName: ZipFileName): String {
    val execOps = project.objects.newInstance(ExecOperations::class.java)
    val outputPath = outputDirectory.value
    return File(outputPath, zipFileName.nameWithExtension)
        .takeIf { it.exists() }
        ?.let { zipFile ->
            ByteArrayOutputStream().use { os ->
                execOps.exec {
                    workingDir = outputPath
                    executable = "swift"
                    args = listOf("package", "compute-checksum", zipFile.name)
                    standardOutput = os
                }
                os.toString()
            }
        } ?: ""
}
