package examples.singletonHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "kind",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = Middle::class, name = "middle"),JsonSubTypes.Type(value = Child::class, name = "child"))
public sealed interface MiddleContract {
  public val kind: String

  public val id: String

  public val note: String?

  public val middle: String?
}
