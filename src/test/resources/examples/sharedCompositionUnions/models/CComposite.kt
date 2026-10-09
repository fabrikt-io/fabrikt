package examples.sharedCompositionUnions.models

import kotlin.Int
import kotlin.String

public interface CComposite {
  public val id: String

  public val kind: CKind

  public val count: Int
}
