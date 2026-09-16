package examples.inlinedEnumParameter.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class NestedFilters(
  @JsonValue
  public val `value`: String,
) {
  NON_UNIQUE_ADDRESSES("non_unique_addresses"),
  PARENT("parent"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, NestedFilters> = entries.associateBy(NestedFilters::value)

    public fun fromValue(`value`: String): NestedFilters? = mapping[value]
  }
}
