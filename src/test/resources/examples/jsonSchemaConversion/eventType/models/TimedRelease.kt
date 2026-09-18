package examples.jsonSchemaConversion.eventType.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import kotlin.Any

public data class TimedRelease(
  @param:JsonProperty("restricted_release_option")
  @get:JsonProperty("restricted_release_option")
  @get:NotNull
  public val restrictedReleaseOption: Any,
  @param:JsonProperty("available_from")
  @get:JsonProperty("available_from")
  @get:NotNull
  public val availableFrom: OffsetDateTime,
) : ReleaseRestrictions
