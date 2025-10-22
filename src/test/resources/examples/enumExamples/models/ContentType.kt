package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ContentType(
  @JsonValue
  public val `value`: String,
) {
  APPLICATION_JSON("application/json"),
  APPLICATION_X_SOME_TYPE_JSON("application/x.some-type+json"),
  APPLICATION_X_SOME_OTHER_TYPE_JSON_VERSION_2("application/x.some-other-type+json;version=2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ContentType> = entries.associateBy(ContentType::value)

    public fun fromValue(`value`: String): ContentType? = mapping[value]
  }
}
