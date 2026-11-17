package examples.allOfParentOrder.models

import kotlin.String
import kotlin.collections.MutableMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

@JsonClassDiscriminator("kind")
@ExperimentalSerializationApi
@Serializable
public sealed class WidenedParent() {
  public abstract val id: String

  public abstract val properties: MutableMap<String, JsonElement>
}
