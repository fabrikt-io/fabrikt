package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = OrderChild::class, name = "child"),JsonSubTypes.Type(value = OrderSibling::class, name = "sibling"),JsonSubTypes.Type(value = OrderTypedChild::class, name = "typed"))
public sealed class OrderX(
  public open val id: String,
  @get:JsonIgnore
  public open val properties: MutableMap<String, Any?> = mutableMapOf(),
) {
  public abstract val kind: String

  @JsonAnyGetter
  public open fun `get`(): Map<String, Any?> = properties

  @JsonAnySetter
  public open fun `set`(name: String, `value`: Any?) {
    properties[name] = value
  }
}
