package examples.githubApi.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class StatusQueryParam(
  @JsonValue
  public val `value`: String,
) {
  ACTIVE("active"),
  INACTIVE("inactive"),
  ALL("all"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, StatusQueryParam> =
        entries.associateBy(StatusQueryParam::value)

    public fun fromValue(`value`: String): StatusQueryParam? = mapping[value]
  }
}
