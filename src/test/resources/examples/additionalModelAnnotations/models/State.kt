package examples.additionalModelAnnotations.models

import com.fasterxml.jackson.`annotation`.JsonValue
import example.annotations.First
import example.annotations.Second
import kotlin.String
import kotlin.collections.Map

@First
@Second
public enum class State(
  @JsonValue
  public val `value`: String,
) {
  OPEN("OPEN"),
  CLOSED("CLOSED"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, State> = entries.associateBy(State::value)

    public fun fromValue(`value`: String): State? = mapping[value]
  }
}
