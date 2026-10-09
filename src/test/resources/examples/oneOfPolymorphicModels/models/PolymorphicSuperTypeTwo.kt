package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "shared",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = PolymorphicTypeTwoA::class, name = "PolymorphicTypeTwoA"),JsonSubTypes.Type(value = PolymorphicTypeTwoB::class, name = "PolymorphicTypeTwoB"))
public sealed class PolymorphicSuperTypeTwo() {
  public abstract val shared: String
}
