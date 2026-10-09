package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "type",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = OneObject::class, name = "OneObject"),JsonSubTypes.Type(value = TwoObject::class, name = "TwoObject"))
public sealed interface SomeObjInlinedObjectNoMappings
