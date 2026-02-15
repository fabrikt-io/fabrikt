package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import kotlin.Any
import kotlin.Int
import kotlin.String

public data class ComplexParent(
  @param:JsonProperty("oneOf")
  @get:JsonProperty("oneOf")
  public val oneOf: Any? = null,
  @param:JsonProperty("first_nested_any_of_prop")
  @get:JsonProperty("first_nested_any_of_prop")
  public val firstNestedAnyOfProp: String? = null,
  @param:JsonProperty("second_nested_any_of_prop")
  @get:JsonProperty("second_nested_any_of_prop")
  public val secondNestedAnyOfProp: String? = null,
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
  @param:JsonProperty("top_level_prop")
  @get:JsonProperty("top_level_prop")
  @get:NotNull
  public val topLevelProp: BigDecimal,
)
