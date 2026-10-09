package examples.directionalModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class ComposedPet(
    @param:JsonProperty(
        "id",
        required = true,
    )
    @get:JsonProperty("id")
    public val id: Int,
    @param:JsonProperty("child")
    @get:JsonProperty("child")
    public val child: Pet? = null,
    @param:JsonProperty("nested")
    @get:JsonProperty("nested")
    public val nested: ComposedPetNested? = null,
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
