package examples.oneOfAllOfBug.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

/**
 * Describes the kind of item and provides a structural hint regarding what optional properties will
 * be present. E.g.: ARTICLE items will always have the article attribute
 */
public enum class GenericItemType(
  @JsonValue
  public val `value`: String,
) {
  LIKED_PRODUCT("LIKED_PRODUCT"),
  PURCHASED_PRODUCT("PURCHASED_PRODUCT"),
  UPLOADED_ITEM("UPLOADED_ITEM"),
  LIKED_OUTFIT("LIKED_OUTFIT"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, GenericItemType> = entries.associateBy(GenericItemType::value)

    public fun fromValue(`value`: String): GenericItemType? = mapping[value]
  }
}
