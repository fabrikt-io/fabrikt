package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class ComposedPetRequest(
    @param:JsonProperty(
        "id",
        required = true,
    )
    @get:JsonProperty("id")
    public val id: Int,
    @param:JsonProperty(
        "age",
        required = true,
    )
    @get:JsonProperty("age")
    public val age: Int,
    @param:JsonProperty("nickname")
    @get:JsonProperty("nickname")
    public val nickname: String? = null,
    @param:JsonProperty("nullableSecret")
    @get:JsonProperty("nullableSecret")
    public val nullableSecret: String?,
    @param:JsonProperty("writeObject")
    @get:JsonProperty("writeObject")
    public val writeObject: ComposedPetWriteObjectRequest,
    @param:JsonProperty("child")
    @get:JsonProperty("child")
    public val child: PetRequest? = null,
    @param:JsonProperty("nested")
    @get:JsonProperty("nested")
    public val nested: ComposedPetNestedRequest? = null,
    @param:JsonProperty("children")
    @get:JsonProperty("children")
    public val children: List<PetRequest>? = null,
    @param:JsonProperty("index")
    @get:JsonProperty("index")
    public val index: Map<String, PetRequest?>? = null,
    @param:JsonProperty("state")
    @get:JsonProperty("state")
    public val state: StateRequest? = null,
)
