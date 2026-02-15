package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class PolymorphicTypeOneB(
  @param:JsonProperty("whateverB")
  @get:JsonProperty("whateverB")
  public val whateverB: Int? = null,
  @get:JsonProperty("shared")
  @get:NotNull
  @param:JsonProperty("shared")
  override val shared: String = "PolymorphicTypeOneB",
) : PolymorphicSuperTypeOne()
