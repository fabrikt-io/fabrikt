package examples.inLinedObject.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String

public data class FirstInlineObjectGeneration(
  @param:JsonProperty("call_home")
  @get:JsonProperty("call_home")
  @get:Valid
  public val callHome: FirstInlineObjectCallHome? = null,
  @param:JsonProperty("database_view")
  @get:JsonProperty("database_view")
  @get:Valid
  public val databaseView: FirstInlineObjectDatabaseView? = null,
  @param:JsonProperty("direct")
  @get:JsonProperty("direct")
  public val direct: String? = null,
)
