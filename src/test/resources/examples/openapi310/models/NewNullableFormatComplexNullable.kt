package examples.openapi310.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "type",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = OneObject::class, name =
    "char_location"),JsonSubTypes.Type(value = TwoObject::class, name = "content_block_location"))
public sealed interface NewNullableFormatComplexNullable
