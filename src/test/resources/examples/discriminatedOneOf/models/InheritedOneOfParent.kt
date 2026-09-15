package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "actionType",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = InheritedOneOfChildOne::class, name =
    "CHILD_ONE"),JsonSubTypes.Type(value = InheritedOneOfChildTwo::class, name = "CHILD_TWO"))
public sealed class InheritedOneOfParent(
  public open val effect: InheritedOneOfParentEffect? = null,
) {
  public abstract val actionType: String
}
