package examples.sharedCompositionExternal.models

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
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  @get:NotNull
  @get:Size(min = 1)
  override val label: String,
  @param:JsonProperty(
    "c",
    required = true,
  )
  @get:JsonProperty("c")
  @get:NotNull
  override val c: Int,
) : Mixed, Shared, AComposite, BaseComposite, CComposite
