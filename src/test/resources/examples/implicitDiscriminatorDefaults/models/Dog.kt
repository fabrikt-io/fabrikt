package examples.implicitDiscriminatorDefaults.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Dog(
  @param:JsonProperty("bark")
  @get:JsonProperty("bark")
  @get:NotNull
  public val bark: String,
  @param:JsonProperty("petType")
  @get:JsonProperty("petType")
  @get:NotNull
  public val petType: String = "Dog",
) : Pet
