package examples.sharedCompositionRequestProjection.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import kotlin.Int

public data class C(
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
