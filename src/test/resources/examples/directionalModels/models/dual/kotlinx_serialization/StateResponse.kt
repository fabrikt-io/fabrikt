package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlin.String
import kotlin.collections.Map

public enum class StateResponse(
    public val `value`: String,
) {
    @SerialName("active")
    ACTIVE("active"),

    @SerialName("inactive")
    INACTIVE("inactive"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, StateResponse> = entries.associateBy(StateResponse::value)

        public fun fromValue(`value`: String): StateResponse? = mapping[value]
    }
}
