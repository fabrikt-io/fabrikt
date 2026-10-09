package examples.sharedCompositionUnions.models

import kotlin.String

public interface OtherComposite {
  public val id: String

  public val kind: OtherKind

  public val message: String
}
