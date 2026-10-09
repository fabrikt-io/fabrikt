package examples.polymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "generation",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = PolymorphicTypeOne::class, name = "PolymorphicTypeOne"),JsonSubTypes.Type(value = PolymorphicTypeTwo::class, name = "polymorphic_type_two"))
public sealed interface PolymorphicType
