package examples.allOfParentOrder.models

import kotlin.String
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@JsonClassDiscriminator("kind")
@ExperimentalSerializationApi
@Serializable
public sealed class UnconfiguredParent() {
  public abstract val id: String
}
