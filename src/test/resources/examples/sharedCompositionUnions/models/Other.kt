package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Other(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  override val id: String,
  @param:JsonProperty("message")
  @get:JsonProperty("message")
  @get:NotNull
  override val message: String,
  @get:JsonProperty("kind")
  @get:NotNull
  @param:JsonProperty("kind")
  override val kind: OtherKind = OtherKind.OTHER,
) : Mixed, OtherComposite
