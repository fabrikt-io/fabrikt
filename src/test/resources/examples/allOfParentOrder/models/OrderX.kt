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
@JsonSubTypes(JsonSubTypes.Type(value = OrderChild::class, name = "child"),JsonSubTypes.Type(value =
    OrderSibling::class, name = "sibling"))
public sealed class OrderX(
  public open val id: String,
) {
  public abstract val kind: String
}
