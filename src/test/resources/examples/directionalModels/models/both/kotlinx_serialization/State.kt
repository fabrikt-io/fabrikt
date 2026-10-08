package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlin.String
import kotlin.collections.Map

public enum class State(
    public val `value`: String,
) {
    @SerialName("active")
    ACTIVE("active"),

    @SerialName("inactive")
    INACTIVE("inactive"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, State> = entries.associateBy(State::value)

        public fun fromValue(`value`: String): State? = mapping[value]
    }
}
