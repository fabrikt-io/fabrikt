package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class PolymorphicTypeTwoB(
  @param:JsonProperty("whateverD")
  @get:JsonProperty("whateverD")
  public val whateverD: Int? = null,
  @get:JsonProperty("shared")
  @get:NotNull
  @param:JsonProperty("shared")
  override val shared: String = "PolymorphicTypeTwoB",
) : PolymorphicSuperTypeTwo()
