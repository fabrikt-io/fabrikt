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
) : Mixed,
    OtherComposite {
  @get:JsonProperty(
    value = "kind",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val kind: OtherKind
    get() = OtherKind.OTHER
}
