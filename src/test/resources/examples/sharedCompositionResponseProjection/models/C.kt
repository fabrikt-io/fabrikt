package examples.sharedCompositionResponseProjection.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.DecimalMin
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
    "code",
    required = true,
  )
  @get:JsonProperty("code")
  @get:NotNull
  @get:DecimalMin(
    value = "1",
    inclusive = true,
  )
  override val code: Int,
) : Choice, AComposite, CComposite
