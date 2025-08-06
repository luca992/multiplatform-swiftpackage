package com.chromaticnoise.multiplatformswiftpackage.task

import com.chromaticnoise.multiplatformswiftpackage.domain.OutputDirectory
import com.chromaticnoise.multiplatformswiftpackage.domain.ZipFileName
import java.io.File

internal fun zipFileChecksum(outputDirectory: OutputDirectory, zipFileName: ZipFileName): String {
    val outputPath = outputDirectory.value
    return File(outputPath, zipFileName.nameWithExtension)
        .takeIf { it.exists() }
        ?.let { zipFile ->
            val processBuilder = ProcessBuilder(
                "swift", "package", "compute-checksum", zipFile.name
            ).apply {
                directory(outputPath)
            }

            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()

            if (exitCode == 0) output else ""
        } ?: ""

}
