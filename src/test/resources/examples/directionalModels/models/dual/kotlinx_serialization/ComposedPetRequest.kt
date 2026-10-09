package examples.directionalModels.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

@Serializable
public data class ComposedPetRequest(
    @SerialName("id")
    public val id: Int,
    @SerialName("age")
    public val age: Int,
    @SerialName("nickname")
    public val nickname: String? = null,
    @SerialName("nullableSecret")
    public val nullableSecret: String?,
    @SerialName("writeObject")
    public val writeObject: ComposedPetWriteObjectRequest,
    @SerialName("child")
    public val child: PetRequest? = null,
    @SerialName("nested")
    public val nested: ComposedPetNestedRequest? = null,
    @SerialName("children")
    public val children: List<PetRequest>? = null,
    @SerialName("index")
    public val index: Map<String, PetRequest?>? = null,
    @SerialName("state")
    public val state: StateRequest? = null,
)
