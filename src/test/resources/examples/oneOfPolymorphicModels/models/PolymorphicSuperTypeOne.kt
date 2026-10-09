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
@JsonSubTypes(JsonSubTypes.Type(value = PolymorphicTypeOneA::class, name = "PolymorphicTypeOneA"),JsonSubTypes.Type(value = PolymorphicTypeOneB::class, name = "PolymorphicTypeOneB"))
public sealed class PolymorphicSuperTypeOne() {
  public abstract val shared: String
}
