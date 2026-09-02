package examples.cookieParameters.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonValue
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.Map

public data class CookiePreferences(
    @param:JsonProperty("locale")
    @get:JsonProperty("locale")
    @get:NotNull
    public val locale: String,
)

public enum class DisplayMode(
    @JsonValue
    public val `value`: String,
) {
    COMPACT("compact"),
    DETAILED("detailed"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, DisplayMode> = entries.associateBy(DisplayMode::value)

        public fun fromValue(`value`: String): DisplayMode? = mapping[value]
    }
}
