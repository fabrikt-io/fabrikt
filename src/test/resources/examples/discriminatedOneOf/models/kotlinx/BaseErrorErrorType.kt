package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class BaseErrorErrorType(
  public val `value`: String,
) {
  @SerialName("VALIDATION")
  VALIDATION("VALIDATION"),
  @SerialName("SERVER")
  SERVER("SERVER"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, BaseErrorErrorType> =
        entries.associateBy(BaseErrorErrorType::value)

    public fun fromValue(`value`: String): BaseErrorErrorType? = mapping[value]
  }
}
