package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class OneOfTwo(
  @param:JsonProperty("oneOfTwo")
  @get:JsonProperty("oneOfTwo")
  public val oneOfTwo: String? = null,
) : ParentOneOf() {
  @get:JsonProperty(
    value = "discriminator",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val discriminator: String
    get() = "OneOfTwo"
}
