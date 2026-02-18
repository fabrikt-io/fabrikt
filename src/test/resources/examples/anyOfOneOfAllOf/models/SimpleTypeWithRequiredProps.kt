package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class SimpleTypeWithRequiredProps(
  @param:JsonProperty("required_string")
  @get:JsonProperty("required_string")
  @get:NotNull
  public val requiredString: String,
  @param:JsonProperty(
    "required_int",
    required = true,
  )
  @get:JsonProperty("required_int")
  @get:NotNull
  public val requiredInt: Int,
)
