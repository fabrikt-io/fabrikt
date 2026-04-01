package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class OneOfTwo(
  @param:JsonProperty("oneOfTwo")
  @get:JsonProperty("oneOfTwo")
  public val oneOfTwo: String? = null,
  @get:JsonProperty("discriminator")
  @get:NotNull
  @param:JsonProperty("discriminator")
  override val discriminator: String = "OneOfTwo",
) : ParentOneOf()
