package examples.defaultValues.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class PersonWithDefaultsEnumDefault(
  @JsonValue
  public val `value`: String,
) {
  TALL("tall"),
  SHORT("short"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, PersonWithDefaultsEnumDefault> =
        entries.associateBy(PersonWithDefaultsEnumDefault::value)

    public fun fromValue(`value`: String): PersonWithDefaultsEnumDefault? = mapping[value]
  }
}
