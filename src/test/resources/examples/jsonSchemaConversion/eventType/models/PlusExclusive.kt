package examples.jsonSchemaConversion.eventType.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import kotlin.Any

public data class PlusExclusive(
  @param:JsonProperty("restricted_release_option")
  @get:JsonProperty("restricted_release_option")
  @get:NotNull
  public val restrictedReleaseOption: Any,
  @param:JsonProperty("plus_exclusive_until")
  @get:JsonProperty("plus_exclusive_until")
  @get:NotNull
  public val plusExclusiveUntil: OffsetDateTime,
) : ReleaseRestrictions
