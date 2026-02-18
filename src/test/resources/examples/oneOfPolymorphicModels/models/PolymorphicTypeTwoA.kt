package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class PolymorphicTypeTwoA(
  @param:JsonProperty("whateverC")
  @get:JsonProperty("whateverC")
  public val whateverC: String? = null,
  @get:JsonProperty("shared")
  @get:NotNull
  @param:JsonProperty("shared")
  override val shared: String = "PolymorphicTypeTwoA",
) : PolymorphicSuperTypeTwo()
