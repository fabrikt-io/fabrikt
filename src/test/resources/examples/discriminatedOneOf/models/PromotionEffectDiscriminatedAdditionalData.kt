package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = PromotionEffectDiscriminatedBuyXGetY::class, name =
    "buyXGetY"),JsonSubTypes.Type(value = PromotionEffectDiscriminatedAutomaticDiscount::class, name
    = "automaticDiscount"))
public sealed interface PromotionEffectDiscriminatedAdditionalData
