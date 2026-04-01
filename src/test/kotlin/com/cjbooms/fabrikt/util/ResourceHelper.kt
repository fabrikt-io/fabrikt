package com.cjbooms.fabrikt.util

import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText

object ResourceHelper {
    fun readTextResource(path: String): String =
        (javaClass.getResource(path) ?: throw FileNotFoundException(path)).readText()

    fun readFolder(path: Path): Map<String, String> =
        path.listDirectoryEntries().filterNot { it.isDirectory() }.associate { it.name to it.readText() }

    fun getFileNamesAndPathsInFolder(path: Path): Map<String, Path> =
        path.listDirectoryEntries().filterNot { it.isDirectory() }.associateBy { it.name }

    fun getFileNamesInFolder(path: Path): List<String> =
         path.listDirectoryEntries().filterNot { it.isDirectory() }.map { it.name }.toList()
}
