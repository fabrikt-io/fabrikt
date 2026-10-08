package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

@Serializable
public data class Pet(
    @SerialName("id")
    public val id: Int,
    @SerialName("child")
    public val child: Pet? = null,
    @SerialName("nested")
    public val nested: PetNested? = null,
    @SerialName("children")
    public val children: List<Pet>? = null,
    @SerialName("index")
    public val index: Map<String, Pet?>? = null,
    @SerialName("state")
    public val state: State? = null,
)
