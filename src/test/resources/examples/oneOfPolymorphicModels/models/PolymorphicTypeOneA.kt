package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class PolymorphicTypeOneA(
  @param:JsonProperty("whateverA")
  @get:JsonProperty("whateverA")
  public val whateverA: String? = null,
) : PolymorphicSuperTypeOne(),
    ContainsOneOfPolymorphicTypesOneOneOf {
  @get:JsonProperty(
    value = "shared",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val shared: String
    get() = "PolymorphicTypeOneA"
}
