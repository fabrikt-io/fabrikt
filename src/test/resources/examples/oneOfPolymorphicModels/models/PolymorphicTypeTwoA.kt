package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class PolymorphicTypeTwoA(
  @param:JsonProperty("whateverC")
  @get:JsonProperty("whateverC")
  public val whateverC: String? = null,
) : PolymorphicSuperTypeTwo(),
    ContainsOneOfPolymorphicTypesOneOneOf {
  @get:JsonProperty(
    value = "shared",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val shared: String
    get() = "PolymorphicTypeTwoA"
}
