package examples.kdoc.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

/**
 * A %-based or fixed-amount discount on your purchase.
 */
public data class Promotion(
    /**
     * The unique identifier of this promotion.
     */
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    public val id: String,
    /**
     * The type of the promotion.
     *
     * - PERCENTAGE: A %-based discount.
     * - FIXED_AMOUNT: A fixed amount discount.
     */
    @param:JsonProperty("type")
    @get:JsonProperty("type")
    public val type: PromotionType,
    /**
     * The name of this promotion.
     */
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    public val name: String? = null,
)

/**
 * The type of the promotion.
 *
 * - PERCENTAGE: A %-based discount.
 * - FIXED_AMOUNT: A fixed amount discount.
 */
public enum class PromotionType(
    @JsonValue
    public val `value`: String,
) {
    PERCENTAGE("PERCENTAGE"),
    FIXED_AMOUNT("FIXED_AMOUNT"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, PromotionType> = entries.associateBy(PromotionType::value)

        public fun fromValue(`value`: String): PromotionType? = mapping[value]
    }
}
