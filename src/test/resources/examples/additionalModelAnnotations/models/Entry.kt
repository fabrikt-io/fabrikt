package examples.additionalModelAnnotations.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import example.annotations.First
import example.annotations.Second
import jakarta.validation.constraints.NotNull
import kotlin.String

@First
@Second
public data class Entry(
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  public val name: String,
  @param:JsonProperty("state")
  @get:JsonProperty("state")
  public val state: State? = null,
) : Choice
