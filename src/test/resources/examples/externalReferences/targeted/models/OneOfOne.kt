package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class OneOfOne(
  @param:JsonProperty("oneOfOne")
  @get:JsonProperty("oneOfOne")
  public val oneOfOne: String? = null,
) : ParentOneOf() {
  @get:JsonProperty(
    value = "discriminator",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val discriminator: String
    get() = "OneOfOne"
}
