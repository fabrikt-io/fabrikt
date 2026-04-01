package examples.inlinedEnumParameter.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class XRequestSource(
  @JsonValue
  public val `value`: String,
) {
  WEB("web"),
  MOBILE("mobile"),
  API("api"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, XRequestSource> = entries.associateBy(XRequestSource::value)

    public fun fromValue(`value`: String): XRequestSource? = mapping[value]
  }
}
