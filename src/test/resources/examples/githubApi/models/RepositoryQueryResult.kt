package examples.githubApi.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.net.URI
import kotlin.collections.List

public data class RepositoryQueryResult(
  /**
   * The hyperlink to a page of data
   */
  @param:JsonProperty("prev")
  @get:JsonProperty("prev")
  public val prev: URI? = null,
  /**
   * The hyperlink to a page of data
   */
  @param:JsonProperty("next")
  @get:JsonProperty("next")
  public val next: URI? = null,
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:NotNull
  @get:Size(min = 0)
  @get:Valid
  public val items: List<Repository>,
)
