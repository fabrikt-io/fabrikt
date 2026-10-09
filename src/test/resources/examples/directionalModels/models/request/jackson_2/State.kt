package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class State(
    @JsonValue
    public val `value`: String,
) {
    ACTIVE("active"),
    INACTIVE("inactive"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, State> = entries.associateBy(State::value)

        public fun fromValue(`value`: String): State? = mapping[value]
    }
}
