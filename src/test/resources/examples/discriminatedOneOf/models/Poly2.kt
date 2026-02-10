package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "type",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = Obj3::class, name = "obj3"),JsonSubTypes.Type(value =
    Obj1::class, name = "obj1"))
public sealed interface Poly2
