package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "petType",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = Dog::class, name = "Dog"),JsonSubTypes.Type(value = Cat::class, name = "Cat"))
public sealed interface Pet
