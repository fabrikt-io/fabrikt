package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ProductStateState(
  @JsonValue
  public val `value`: String,
) {
  LIVE("live"),
  DRAFT("draft"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ProductStateState> =
        entries.associateBy(ProductStateState::value)

    public fun fromValue(`value`: String): ProductStateState? = mapping[value]
  }
}
