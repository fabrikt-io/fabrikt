package examples.sharedCompositionExternal.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Boolean
import kotlin.Int
import kotlin.String

public data class G(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
  @param:JsonProperty("enabled")
  @get:JsonProperty("enabled")
  @get:NotNull
  override val enabled: Boolean = true,
  @param:JsonProperty(
    "g",
    required = true,
  )
  @get:JsonProperty("g")
  @get:NotNull
  public val g: Int,
) : StandaloneComposite
