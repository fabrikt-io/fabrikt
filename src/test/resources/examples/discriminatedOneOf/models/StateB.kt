package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "status",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = StateB1::class, name = "b1"),JsonSubTypes.Type(value =
    StateB2::class, name = "b2"))
public sealed interface StateB : State
