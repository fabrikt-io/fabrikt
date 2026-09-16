package examples.propertyPathRefParameter.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonValue
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class DictValue(
    @param:JsonProperty("v")
    @get:JsonProperty("v")
    public val v: String? = null,
)

public data class Product(
    @param:JsonProperty("title")
    @get:JsonProperty("title")
    public val title: String? = null,
    @param:JsonProperty("state")
    @get:JsonProperty("state")
    public val state: ProductState? = null,
    @param:JsonProperty("meta")
    @get:JsonProperty("meta")
    @get:Valid
    public val meta: ProductMeta? = null,
    @param:JsonProperty("tags")
    @get:JsonProperty("tags")
    public val tags: List<ProductTags>? = null,
    @param:JsonProperty("dict")
    @get:JsonProperty("dict")
    @get:Valid
    public val dict: Map<String, DictValue?>? = null,
)

public data class ProductMeta(
    @param:JsonProperty("note")
    @get:JsonProperty("note")
    public val note: String? = null,
)

public enum class ProductState(
    @JsonValue
    public val `value`: String,
) {
    LIVE("live"),
    DRAFT("draft"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, ProductState> = entries.associateBy(ProductState::value)

        public fun fromValue(`value`: String): ProductState? = mapping[value]
    }
}

public enum class ProductTags(
    @JsonValue
    public val `value`: String,
) {
    NEW("new"),
    SALE("sale"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, ProductTags> = entries.associateBy(ProductTags::value)

        public fun fromValue(`value`: String): ProductTags? = mapping[value]
    }
}
