package examples.leadingUnderscoreProperty.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public data class MyCustomType(
  @param:JsonProperty(
    "id",
    required = true,
  )
  @get:JsonProperty("id")
  @get:NotNull
  public val id: Long,
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  public val name: String,
  @param:JsonProperty("enabled")
  @get:JsonProperty("enabled")
  public val enabled: Boolean? = null,
)
