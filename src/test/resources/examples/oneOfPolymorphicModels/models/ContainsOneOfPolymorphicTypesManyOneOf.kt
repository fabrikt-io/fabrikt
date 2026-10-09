package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "generation",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = PolymorphicTypeOneB::class, name = "PolymorphicTypeOneB"),JsonSubTypes.Type(value = PolymorphicTypeTwoB::class, name = "PolymorphicTypeTwoB"))
public sealed interface ContainsOneOfPolymorphicTypesManyOneOf
