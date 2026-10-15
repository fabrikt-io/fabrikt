package examples.sharedCompositionMappedContracts.models

import kotlin.String

public interface AComposite {
  public val id: String

  public val details: Details

  public val serverValue: String?

  public val secret: String?
}
