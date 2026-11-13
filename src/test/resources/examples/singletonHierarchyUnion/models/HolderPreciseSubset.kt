package examples.singletonHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = Child::class, name = "child"),JsonSubTypes.Type(value = Sibling::class, name = "sibling"))
public sealed interface HolderPreciseSubset
