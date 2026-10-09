package examples.sharedCompositionRefinements.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = Cat::class, name = "cat"),JsonSubTypes.Type(value =
    Dog::class, name = "dog"))
public sealed class Pet(
  open override val id: String,
) : PetComposite {
  abstract override val kind: String
}
