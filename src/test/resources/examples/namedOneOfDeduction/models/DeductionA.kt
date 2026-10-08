package examples.namedOneOfDeduction.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class DeductionA(
  @param:JsonProperty("a")
  @get:JsonProperty("a")
  @get:NotNull
  @get:Size(min = 1)
  public val a: String,
) : DeductionChoice
