package examples.validationAnnotations.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String
import kotlin.collections.List

public data class ValidationAnnotations(
  @param:JsonProperty("user_name")
  @get:JsonProperty("user_name")
  @get:NotNull
  @get:Pattern(regexp = "[a-zA-Z]")
  public val userName: String,
  @param:JsonProperty(
    "age",
    required = true,
  )
  @get:JsonProperty("age")
  @get:NotNull
  @get:DecimalMin(
    value = "0",
    inclusive = false,
  )
  @get:DecimalMax(
    value = "100",
    inclusive = true,
  )
  public val age: Int,
  @param:JsonProperty("bio")
  @get:JsonProperty("bio")
  @get:NotNull
  @get:Size(
    min = 20,
    max = 200,
  )
  public val bio: String,
  @param:JsonProperty("friends")
  @get:JsonProperty("friends")
  @get:NotNull
  @get:Size(
    min = 0,
    max = 10,
  )
  public val friends: List<String>,
)
