package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class FooBarsFoo(
  @JsonValue
  public val `value`: String,
) {
  X("X"),
  Y("Y"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, FooBarsFoo> = entries.associateBy(FooBarsFoo::value)

    public fun fromValue(`value`: String): FooBarsFoo? = mapping[value]
  }
}
