package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = B::class, name = "b"),JsonSubTypes.Type(value = C::class,
    name = "c"),JsonSubTypes.Type(value = Other::class, name = "other"))
public sealed interface Mixed
