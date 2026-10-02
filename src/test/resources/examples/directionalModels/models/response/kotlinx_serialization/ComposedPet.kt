package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

@Serializable
public data class ComposedPet(
    @SerialName("id")
    public val id: Int,
    @SerialName("name")
    public val name: String,
    @SerialName("readList")
    public val readList: List<String>,
    @SerialName("readMap")
    public val readMap: Map<String, String?>? = null,
    @SerialName("child")
    public val child: Pet? = null,
    @SerialName("nested")
    public val nested: ComposedPetNested? = null,
    @SerialName("children")
    public val children: List<Pet>? = null,
    @SerialName("index")
    public val index: Map<String, Pet?>? = null,
    @SerialName("state")
    public val state: State? = null,
    @SerialName("serial")
    public val serial: String,
)
