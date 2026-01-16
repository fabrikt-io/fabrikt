package examples.defaultValues.models

import com.fasterxml.jackson.`annotation`.JsonInclude
import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int
import kotlin.String

public data class PersonWithDefaultsIgnoredObjectDefault(
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val name: String? = null,
  @param:JsonProperty("age")
  @get:JsonProperty("age")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val age: Int? = null,
)
