package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "errorType",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = ValidationErr::class, name = "VALIDATION"),JsonSubTypes.Type(value = ServerErr::class, name = "SERVER"))
public sealed interface ErrorWrapperDiscriminatedError
