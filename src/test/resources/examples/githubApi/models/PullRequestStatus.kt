package examples.githubApi.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

/**
 * States whether the entity is currently active or inactive
 */
public enum class PullRequestStatus(
  @JsonValue
  public val `value`: String,
) {
  ACTIVE("active"),
  INACTIVE("inactive"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, PullRequestStatus> =
        entries.associateBy(PullRequestStatus::value)

    public fun fromValue(`value`: String): PullRequestStatus? = mapping[value]
  }
}
