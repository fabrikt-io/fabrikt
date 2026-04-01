package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "actionType",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = ChildActionA::class, name =
    "CHILD_A"),JsonSubTypes.Type(value = ChildActionB::class, name = "CHILD_B"))
public sealed class ParentAction() {
  public abstract val actionType: ParentActionActionType
}
