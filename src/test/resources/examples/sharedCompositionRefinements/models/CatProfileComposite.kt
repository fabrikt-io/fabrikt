package examples.sharedCompositionRefinements.models

import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public interface CatProfileComposite {
  public val pet: Cat

  public val pets: List<Cat>

  public val petsByName: Map<String, Cat?>

  public val optionalPet: Cat

  public val note: String
}
