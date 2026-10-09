package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class StateRequest(
    @JsonValue
    public val `value`: String,
) {
    ACTIVE("active"),
    INACTIVE("inactive"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, StateRequest> = entries.associateBy(StateRequest::value)

        public fun fromValue(`value`: String): StateRequest? = mapping[value]
    }
}
