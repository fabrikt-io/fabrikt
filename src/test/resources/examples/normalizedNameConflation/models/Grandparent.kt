package examples.normalizedNameConflation.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "${'$'}type",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = Parent::class, name = "parent"))
public sealed class Grandparent() {
  public abstract val `$type`: String
}
