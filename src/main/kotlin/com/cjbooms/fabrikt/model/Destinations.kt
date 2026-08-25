package com.cjbooms.fabrikt.model

import java.nio.file.Path
import java.nio.file.Paths

object Destinations {
    private val PATH_ROOT: Path = Paths.get("")
    val MAIN_KT_SOURCE: Path = PATH_ROOT.resolve("src/main/kotlin/")
    val MAIN_RESOURCES: Path = PATH_ROOT.resolve("src/main/resources")

    fun modelsPackage(basePackage: String): String = "$basePackage.models"

    fun controllersPackage(basePackage: String): String = "$basePackage.controllers"

    fun clientPackage(basePackage: String) = "$basePackage.client"
}
