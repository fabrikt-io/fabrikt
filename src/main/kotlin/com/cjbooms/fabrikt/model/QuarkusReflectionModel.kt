package com.cjbooms.fabrikt.model

data class QuarkusReflectionModel(
    val name: String,
    val allDeclaredConstructors: Boolean = true,
    val allPublicConstructors: Boolean = true,
    val allDeclaredMethods: Boolean = true,
    val allPublicMethods: Boolean = true,
    val allDeclaredFields: Boolean = true,
    val allPublicFields: Boolean = true,
)
