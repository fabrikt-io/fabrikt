package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "type",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = ChildTypeA::class, name = "CHILD_TYPE_A"),JsonSubTypes.Type(value = ChildTypeB::class, name = "CHILD_TYPE_B"))
public sealed class ParentSpec() {
  /**
   * Shows which child type is being returned
   */
  public abstract val type: ParentType
}
