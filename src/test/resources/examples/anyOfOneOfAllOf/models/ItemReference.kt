package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "item_type",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = ProductItem::class, name =
    "PRODUCT"),JsonSubTypes.Type(value = OutfitItem::class, name = "OUTFIT"))
public sealed interface ItemReference
