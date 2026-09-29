package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

@Serializable
public data class ComposedPetResponse(
    @SerialName("id")
    public val id: Int,
    @SerialName("name")
    public val name: String,
    @SerialName("readList")
    public val readList: List<String>,
    @SerialName("readMap")
    public val readMap: Map<String, String?>? = null,
    @SerialName("child")
    public val child: PetResponse? = null,
    @SerialName("nested")
    public val nested: ComposedPetNestedResponse? = null,
    @SerialName("children")
    public val children: List<PetResponse>? = null,
    @SerialName("index")
    public val index: Map<String, PetResponse?>? = null,
    @SerialName("state")
    public val state: StateResponse? = null,
    @SerialName("serial")
    public val serial: String,
)
