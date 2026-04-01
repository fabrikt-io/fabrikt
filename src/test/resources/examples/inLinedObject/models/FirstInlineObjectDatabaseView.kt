package examples.inLinedObject.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class FirstInlineObjectDatabaseView(
  @param:JsonProperty("view_name")
  @get:JsonProperty("view_name")
  @get:NotNull
  public val viewName: String,
)
