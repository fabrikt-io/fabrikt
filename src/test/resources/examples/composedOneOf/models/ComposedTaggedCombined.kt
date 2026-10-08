package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = ComposedTaggedCombinedComposedTaggedA::class, name =
    "a"),JsonSubTypes.Type(value = ComposedTaggedCombinedComposedTaggedB::class, name = "b"))
public sealed interface ComposedTaggedCombined
