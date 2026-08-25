package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Models
import com.squareup.kotlinpoet.FileSpec

object TestFileUtils {
    fun Collection<ClientType>.toSingleFile(): String {
        val destPackage = if (this.isNotEmpty()) first().destinationPackage else ""
        val singleFileBuilder = FileSpec.builder(destPackage, "dummyFilename")
        this.forEach {
            singleFileBuilder.addType(it.spec)
            it.imports.forEach { (pkg, name) -> singleFileBuilder.addImport(pkg, name) }
        }
        return Linter.lintString(singleFileBuilder.build().toString())
    }

    fun Models.toSingleFile(): String {
        val destPackage = if (models.isNotEmpty()) models.first().destinationPackage else ""
        val singleFileBuilder = FileSpec.builder(destPackage, "dummyFilename")
        models
            .sortedBy { it.spec.name }
            .forEach {
                val builder =
                    singleFileBuilder
                        .addType(it.spec)
                builder.build()
            }
        return Linter.lintString(singleFileBuilder.build().toString())
    }
}
