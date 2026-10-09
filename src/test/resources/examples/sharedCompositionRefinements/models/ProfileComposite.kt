package examples.sharedCompositionRefinements.models

import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public interface ProfileComposite {
  public val pet: Pet

  public val pets: List<Pet>

  public val petsByName: Map<String, Pet?>

  public val optionalPet: Pet?

  public val note: String
}
