package examples.redundantHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = First::class, name = "first"),JsonSubTypes.Type(value = Second::class, name = "second"))
public sealed class Base(
  public open val id: String,
  public open val description: String? = null,
) {
  public abstract val kind: String
}
