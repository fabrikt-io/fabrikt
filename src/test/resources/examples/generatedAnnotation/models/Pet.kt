package com.example.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import javax.`annotation`.processing.Generated
import kotlin.Long
import kotlin.String

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public data class Pet(
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    public val id: Long? = null,
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    @get:NotNull
    public val name: String,
    @param:JsonProperty("tag")
    @get:JsonProperty("tag")
    @get:NotNull
    public val tag: String,
    @param:JsonProperty("type")
    @get:JsonProperty("type")
    public val type: PetType? = null,
)
