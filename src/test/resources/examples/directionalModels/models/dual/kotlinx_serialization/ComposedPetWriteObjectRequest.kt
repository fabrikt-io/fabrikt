package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.String

@Serializable
public data class ComposedPetWriteObjectRequest(
    @SerialName("token")
    public val token: String,
)
