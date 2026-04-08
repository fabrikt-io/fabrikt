package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class BaseErrorErrorType(
  @JsonValue
  public val `value`: String,
) {
  VALIDATION("VALIDATION"),
  SERVER("SERVER"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, BaseErrorErrorType> =
        entries.associateBy(BaseErrorErrorType::value)

    public fun fromValue(`value`: String): BaseErrorErrorType? = mapping[value]
  }
}
