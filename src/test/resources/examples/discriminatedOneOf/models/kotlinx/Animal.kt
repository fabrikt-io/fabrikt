package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class Animal(
  public val `value`: String,
) {
  @SerialName("Moo")
  MOO("Moo"),
  @SerialName("Baa")
  BAA("Baa"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Animal> = entries.associateBy(Animal::value)

    public fun fromValue(`value`: String): Animal? = mapping[value]
  }
}
