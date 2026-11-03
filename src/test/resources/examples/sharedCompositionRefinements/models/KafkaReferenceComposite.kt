package examples.sharedCompositionRefinements.models

import kotlin.String

public interface KafkaReferenceComposite {
  public val type: SinkType

  public val label: String?
}
