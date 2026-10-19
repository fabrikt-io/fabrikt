package examples.externalPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "some_enum",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = ConcreteImplOne::class, name = "obj_one_only"),JsonSubTypes.Type(value = ConcreteImplTwo::class, name = "obj_two_first"),JsonSubTypes.Type(value = ConcreteImplTwo::class, name = "obj_two_second"),JsonSubTypes.Type(value = ConcreteImplThree::class, name = "obj_three"))
public sealed class PolymorphicEnumDiscriminator() {
  public abstract val someEnum: EnumDiscriminator
}
