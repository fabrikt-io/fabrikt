package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ProductStateExtra(
  @JsonValue
  public val `value`: String,
) {
  LIVE("live"),
  DRAFT("draft"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ProductStateExtra> =
        entries.associateBy(ProductStateExtra::value)

    public fun fromValue(`value`: String): ProductStateExtra? = mapping[value]
  }
}
