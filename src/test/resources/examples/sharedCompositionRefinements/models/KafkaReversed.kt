package examples.sharedCompositionRefinements.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class KafkaReversed(
  @param:JsonProperty("type")
  @get:JsonProperty("type")
  @get:NotNull
  override val type: SinkType = SinkType.KAFKA,
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  @get:Size(
    min = 2,
    max = 10,
  )
  override val label: String? = null,
) : SinkFieldsComposite
