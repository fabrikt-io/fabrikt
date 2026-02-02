package examples.openapi310.models

import com.fasterxml.jackson.`annotation`.JsonInclude
import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.Valid

public data class SingleRequiredFieldNullableObject(
  @param:JsonProperty("requiredNullableRef")
  @get:JsonProperty("requiredNullableRef")
  @get:Valid
  @param:JsonInclude(JsonInclude.Include.ALWAYS)
  public val requiredNullableRef: OneObject?,
)
