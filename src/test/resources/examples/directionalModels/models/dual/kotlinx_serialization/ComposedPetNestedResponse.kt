package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.String

@Serializable
public data class ComposedPetNestedResponse(
    @SerialName("code")
    public val code: String,
)
