package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = TypedPlainChild::class, name = "plainChild"),JsonSubTypes.Type(value = TypedStringChild::class, name = "stringChild"))
public sealed class TypedParent(
  public open val id: String,
  @get:JsonIgnore
  public open val properties: MutableMap<String, String?> = mutableMapOf(),
) {
  public abstract val kind: String

  @JsonAnyGetter
  public open fun `get`(): Map<String, String?> = properties

  @JsonAnySetter
  public open fun `set`(name: String, `value`: String?) {
    properties[name] = value
  }
}
