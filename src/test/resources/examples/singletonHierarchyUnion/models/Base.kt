package examples.singletonHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = Middle::class, name = "middle"),JsonSubTypes.Type(value = Child::class, name = "child"),JsonSubTypes.Type(value = Sibling::class, name = "sibling"),JsonSubTypes.Type(value = Third::class, name = "third"))
public sealed class Base(
  public open val id: String,
  public open val note: String? = "hello",
) {
  public abstract val kind: String
}
