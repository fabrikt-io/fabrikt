package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String

public data class C(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
  @param:JsonProperty(
    "count",
    required = true,
  )
  @get:JsonProperty("count")
  @get:NotNull
  override val count: Int,
) : Mixed,
    Shared,
    AComposite,
    CComposite {
  @get:JsonProperty(
    value = "kind",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val kind: CKind
    get() = CKind.C
}
