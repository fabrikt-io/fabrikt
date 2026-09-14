package examples.customTypeMapping.models

import examples.customTypeMapping.DurationAsIsoStringSerializer
import jakarta.validation.constraints.NotNull
import java.time.Duration
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Content(
  @Serializable(with = DurationAsIsoStringSerializer::class)
  @SerialName("id")
  @get:NotNull
  public val id: Duration,
  @SerialName("ids")
  @get:NotNull
  public val ids: List<@Serializable(with = DurationAsIsoStringSerializer::class) Duration>,
  @SerialName("durations")
  @get:NotNull
  public val durations: Map<String, @Serializable(with = DurationAsIsoStringSerializer::class)
      Duration?>,
)
