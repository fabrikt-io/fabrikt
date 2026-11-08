package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class Sheep(
  public val `value`: String,
) {
  @SerialName("Baa")
  BAA("Baa"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Sheep> = entries.associateBy(Sheep::value)

    public fun fromValue(`value`: String): Sheep? = mapping[value]
  }
}
