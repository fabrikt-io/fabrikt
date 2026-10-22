package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class PolymorphicTypeTwoB(
  @param:JsonProperty("whateverD")
  @get:JsonProperty("whateverD")
  public val whateverD: Int? = null,
) : PolymorphicSuperTypeTwo(),
    ContainsOneOfPolymorphicTypesManyOneOf {
  @get:JsonProperty(
    value = "shared",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val shared: String
    get() = "PolymorphicTypeTwoB"
}
