package examples.inlinedEnumParameter.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class SortOrder(
  @JsonValue
  public val `value`: String,
) {
  ASC("asc"),
  DESC("desc"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SortOrder> = entries.associateBy(SortOrder::value)

    public fun fromValue(`value`: String): SortOrder? = mapping[value]
  }
}
