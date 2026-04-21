package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "errorCode",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = DiscriminatedChild::class, name = "ERR_ONE"))
public sealed class DiscriminatedBase(
  public open val message: String,
) {
  public abstract val errorCode: String
}
