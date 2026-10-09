package examples.sharedCompositionExternal.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Boolean
import kotlin.String

public data class F(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
  @param:JsonProperty("enabled")
  @get:JsonProperty("enabled")
  @get:NotNull
  override val enabled: Boolean = true,
  @param:JsonProperty("f")
  @get:JsonProperty("f")
  @get:NotNull
  public val f: String,
) : StandaloneComposite
