package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class Status(
  @JsonValue
  public val `value`: String,
) {
  A("a"),
  B("b"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Status> = entries.associateBy(Status::value)

    public fun fromValue(`value`: String): Status? = mapping[value]
  }
}
