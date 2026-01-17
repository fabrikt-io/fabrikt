package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class SetWithInlinedAllOfItems(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  public val id: String,
  @param:JsonProperty(
    "status_code",
    required = true,
  )
  @get:JsonProperty("status_code")
  @get:NotNull
  public val statusCode: Int,
)
