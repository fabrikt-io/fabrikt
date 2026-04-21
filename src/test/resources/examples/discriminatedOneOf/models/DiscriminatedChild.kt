package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class DiscriminatedChild(
  @param:JsonProperty("message")
  @get:JsonProperty("message")
  @get:NotNull
  override val message: String,
  @get:JsonProperty("errorCode")
  @get:NotNull
  @param:JsonProperty("errorCode")
  override val errorCode: String = "ERR_ONE",
) : DiscriminatedBase(message)
