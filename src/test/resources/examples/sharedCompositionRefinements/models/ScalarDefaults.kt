package examples.sharedCompositionRefinements.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String

public data class ScalarDefaults(
  @param:JsonProperty("text")
  @get:JsonProperty("text")
  public val text: String? = null,
  @param:JsonProperty("flag")
  @get:JsonProperty("flag")
  public val flag: Boolean? = null,
  @param:JsonProperty("count")
  @get:JsonProperty("count")
  public val count: Int? = null,
  @param:JsonProperty("number")
  @get:JsonProperty("number")
  public val number: BigDecimal? = null,
  @param:JsonProperty("booleanText")
  @get:JsonProperty("booleanText")
  public val booleanText: String? = null,
  @param:JsonProperty("overflowFloat")
  @get:JsonProperty("overflowFloat")
  public val overflowFloat: Float? = null,
  @param:JsonProperty("validFlag")
  @get:JsonProperty("validFlag")
  @get:NotNull
  public val validFlag: Boolean = true,
  @param:JsonProperty("validCount")
  @get:JsonProperty("validCount")
  @get:NotNull
  public val validCount: Int = 7,
  @param:JsonProperty("validLong")
  @get:JsonProperty("validLong")
  @get:NotNull
  public val validLong: Long = 4294967296,
)
