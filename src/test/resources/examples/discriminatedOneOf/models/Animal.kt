package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class Animal(
  @JsonValue
  public val `value`: String,
) {
  MOO("Moo"),
  BAA("Baa"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Animal> = entries.associateBy(Animal::value)

    public fun fromValue(`value`: String): Animal? = mapping[value]
  }
}
