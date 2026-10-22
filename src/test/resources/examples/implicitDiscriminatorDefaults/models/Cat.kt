package examples.implicitDiscriminatorDefaults.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Cat(
  @param:JsonProperty("meow")
  @get:JsonProperty("meow")
  @get:NotNull
  public val meow: String,
  @param:JsonProperty("petType")
  @get:JsonProperty("petType")
  @get:NotNull
  public val petType: String = "Cat",
) : Pet
