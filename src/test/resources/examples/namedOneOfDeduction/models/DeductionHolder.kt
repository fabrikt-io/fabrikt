package examples.namedOneOfDeduction.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class DeductionHolder(
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  @get:NotNull
  @get:Valid
  public val `value`: DeductionChoice,
  @param:JsonProperty("choices")
  @get:JsonProperty("choices")
  @get:Valid
  public val choices: List<DeductionChoice>? = null,
  @param:JsonProperty("byKey")
  @get:JsonProperty("byKey")
  @get:Valid
  public val byKey: Map<String, DeductionChoice?>? = null,
  @param:JsonProperty("optional")
  @get:JsonProperty("optional")
  @get:Valid
  public val optional: DeductionChoice? = null,
)
