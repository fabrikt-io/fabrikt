package examples.enumPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.collections.List

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "some_enum",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = DiscriminatedChild1::class, name = "obj_one_only"),JsonSubTypes.Type(value = DiscriminatedChild2::class, name = "obj_two_first"),JsonSubTypes.Type(value = DiscriminatedChild2::class, name = "obj_two_second"),JsonSubTypes.Type(value = DiscriminatedChild3::class, name = "obj_three"))
public sealed class ChildDefinition(
  public open val inlineObj: ChildDefinitionInlineObj? = null,
  public open val inlineArray: List<ChildDefinitionInlineArray>? = null,
  public open val inlineEnum: ChildDefinitionInlineEnum? = null,
) {
  public abstract val someEnum: ChildDiscriminator
}
