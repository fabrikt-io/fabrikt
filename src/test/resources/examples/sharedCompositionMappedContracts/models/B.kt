package examples.sharedCompositionMappedContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class B(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 1)
  override val id: String,
  @param:JsonProperty("details")
  @get:JsonProperty("details")
  @get:NotNull
  @get:Valid
  override val details: Details,
  @param:JsonProperty("serverValue")
  @get:JsonProperty("serverValue")
  override val serverValue: String? = null,
  @param:JsonProperty("secret")
  @get:JsonProperty("secret")
  override val secret: String? = null,
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  public val label: String? = null,
) : AComposite
