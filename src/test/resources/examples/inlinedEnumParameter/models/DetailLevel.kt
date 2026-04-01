package examples.inlinedEnumParameter.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class DetailLevel(
  @JsonValue
  public val `value`: String,
) {
  SUMMARY("summary"),
  FULL("full"),
  VERBOSE("verbose"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, DetailLevel> = entries.associateBy(DetailLevel::value)

    public fun fromValue(`value`: String): DetailLevel? = mapping[value]
  }
}
