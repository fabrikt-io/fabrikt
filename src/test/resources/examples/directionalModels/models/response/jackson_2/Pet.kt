package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class Pet(
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
    public val child: Pet? = null,
    @param:JsonProperty("nested")
    @get:JsonProperty("nested")
    public val nested: PetNested? = null,
    @param:JsonProperty("children")
    @get:JsonProperty("children")
    public val children: List<Pet>? = null,
    @param:JsonProperty("index")
    @get:JsonProperty("index")
    public val index: Map<String, Pet?>? = null,
    @param:JsonProperty("state")
    @get:JsonProperty("state")
    public val state: State? = null,
)
