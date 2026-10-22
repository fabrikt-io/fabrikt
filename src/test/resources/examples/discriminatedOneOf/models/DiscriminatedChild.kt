package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class DiscriminatedChild(
  @param:JsonProperty("message")
  @get:JsonProperty("message")
  @get:NotNull
  override val message: String,
) : DiscriminatedBase(message) {
  @get:JsonProperty(
    value = "errorCode",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val errorCode: String
    get() = "ERR_ONE"
}
