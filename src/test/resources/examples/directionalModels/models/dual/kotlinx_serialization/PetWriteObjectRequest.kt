package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.String

@Serializable
public data class PetWriteObjectRequest(
    @SerialName("token")
    public val token: String,
)
