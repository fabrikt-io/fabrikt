package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class PolymorphicTypeOneB(
  @param:JsonProperty("whateverB")
  @get:JsonProperty("whateverB")
  public val whateverB: Int? = null,
) : PolymorphicSuperTypeOne(),
    ContainsOneOfPolymorphicTypesManyOneOf {
  @get:JsonProperty(
    value = "shared",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val shared: String
    get() = "PolymorphicTypeOneB"
}
