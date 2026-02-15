package examples.githubApi.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Webhook(
  @param:JsonProperty("url")
  @get:JsonProperty("url")
  @get:NotNull
  public val url: String,
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  public val name: String? = null,
)
