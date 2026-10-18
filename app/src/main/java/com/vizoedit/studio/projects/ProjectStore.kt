package com.vizoedit.studio.projects

import android.content.Context
import java.io.File
import java.io.IOException

/** Stores validated project metadata in app-private storage. */
class ProjectStore(context: Context) {
    private val directory = File(context.filesDir, "projects").apply { if (!exists() && !mkdirs()) throw IOException("Cannot create project directory") }

    @Throws(IOException::class)
    fun save(project: ProjectDocument): File {
        project.validate()
        val safeName = project.name.lowercase().replace(Regex("[^\\p{L}\\p{N}_-]+"), "-").trim('-').take(48).ifBlank { "project" }
        val destination = File(directory, "$safeName.vizoproject")
        val temp = File(directory, "$safeName.tmp")
        try {
            temp.writeText(project.toJson(), Charsets.UTF_8)
            try {
                java.nio.file.Files.move(
                    temp.toPath(), destination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE
                )
            } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
                java.nio.file.Files.move(temp.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            }
            return destination
        } finally { if (temp.exists()) temp.delete() }
    }

    @Throws(IOException::class)
    fun load(file: File): ProjectDocument {
        require(file.canonicalFile.parentFile == directory.canonicalFile) { "Project file is outside the project folder" }
        if (!file.isFile || file.length() > 1_000_000L) throw IOException("Project file is missing or too large")
        return try { ProjectDocument.fromJson(file.readText(Charsets.UTF_8)) }
        catch (e: Exception) { throw IOException("Project file is invalid", e) }
    }

    fun list(): List<File> = directory.listFiles { file -> file.isFile && file.extension == "vizoproject" }
        ?.sortedByDescending { it.lastModified() } ?: emptyList()
}
