package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class Foo(
  @JsonValue
  public val `value`: String,
) {
  X("X"),
  Y("Y"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Foo> = entries.associateBy(Foo::value)

    public fun fromValue(`value`: String): Foo? = mapping[value]
  }
}
