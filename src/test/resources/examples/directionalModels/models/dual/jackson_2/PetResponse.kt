package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class PetResponse(
    @param:JsonProperty(
        "id",
        required = true,
    )
    @get:JsonProperty("id")
    public val id: Int,
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    public val name: String,
    @param:JsonProperty("readList")
    @get:JsonProperty("readList")
    public val readList: List<String>,
    @param:JsonProperty("readMap")
    @get:JsonProperty("readMap")
    public val readMap: Map<String, String?>? = null,
    @param:JsonProperty("child")
    @get:JsonProperty("child")
    public val child: PetResponse? = null,
    @param:JsonProperty("nested")
    @get:JsonProperty("nested")
    public val nested: PetNestedResponse? = null,
    @param:JsonProperty("children")
    @get:JsonProperty("children")
    public val children: List<PetResponse>? = null,
    @param:JsonProperty("index")
    @get:JsonProperty("index")
    public val index: Map<String, PetResponse?>? = null,
    @param:JsonProperty("state")
    @get:JsonProperty("state")
    public val state: StateResponse? = null,
)
