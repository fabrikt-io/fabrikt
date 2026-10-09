package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String

public data class DeductionC(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
  @param:JsonProperty(
    "c",
    required = true,
  )
  @get:JsonProperty("c")
  @get:NotNull
  override val c: Int,
) : DeductionChoice, AComposite, DeductionCComposite
