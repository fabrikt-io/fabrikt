package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = ExplicitChild::class, name = "explicit"),JsonSubTypes.Type(value = QuietSibling::class, name = "quiet"))
public sealed class UnconfiguredParent(
  public open val id: String,
) {
  public abstract val kind: String
}
