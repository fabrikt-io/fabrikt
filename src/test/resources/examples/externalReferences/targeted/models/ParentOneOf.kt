package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "discriminator",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = OneOfOne::class, name = "OneOfOne"),JsonSubTypes.Type(value = OneOfTwo::class, name = "OneOfTwo"))
public sealed class ParentOneOf() {
  public abstract val discriminator: String
}
