package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.String

@Serializable
public data class ReadOnlyRecordResponse(
    @SerialName("value")
    public val `value`: String,
)
