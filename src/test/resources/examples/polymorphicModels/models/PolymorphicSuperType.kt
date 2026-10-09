package examples.polymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.collections.List

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "generation",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = PolymorphicTypeOne::class, name = "PolymorphicTypeOne"),JsonSubTypes.Type(value = PolymorphicTypeTwo::class, name = "polymorphic_type_two"))
public sealed class PolymorphicSuperType(
  public open val firstName: String,
  public open val lastName: String,
  public open val pets: List<Pet>,
) {
  public abstract val generation: String
}
