package examples.requiredReadOnly.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import kotlin.String

public data class RequiredReadOnly(
  @param:JsonProperty("user_name")
  @get:JsonProperty("user_name")
  @get:NotNull
  public val userName: String,
  @param:JsonProperty("created")
  @get:JsonProperty("created")
  public val created: OffsetDateTime? = null,
  @param:JsonProperty("created_by")
  @get:JsonProperty("created_by")
  public val createdBy: String? = null,
)
