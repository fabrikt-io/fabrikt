package examples.openapi310.models

import com.fasterxml.jackson.`annotation`.JsonInclude
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List

public data class NewNullableFormat(
  /**
   * The resolved version or `null` if there is no matching version.
   */
  @param:JsonProperty("simpleNullable")
  @get:JsonProperty("simpleNullable")
  @param:JsonInclude(JsonInclude.Include.ALWAYS)
  public val simpleNullable: String?,
  @param:JsonProperty("objectNullable")
  @get:JsonProperty("objectNullable")
  @get:Valid
  public val objectNullable: OneObject? = null,
  @param:JsonProperty("requiredNullableRef")
  @get:JsonProperty("requiredNullableRef")
  @get:Valid
  @param:JsonInclude(JsonInclude.Include.ALWAYS)
  public val requiredNullableRef: OneObject?,
  @param:JsonProperty("singleRequiredFieldNullableRef")
  @get:JsonProperty("singleRequiredFieldNullableRef")
  @get:Valid
  @param:JsonInclude(JsonInclude.Include.ALWAYS)
  public val singleRequiredFieldNullableRef: SingleRequiredFieldNullableObject?,
  @param:JsonProperty("complexNullable")
  @get:JsonProperty("complexNullable")
  @get:Valid
  public val complexNullable: List<NewNullableFormatComplexNullable>? = null,
)
