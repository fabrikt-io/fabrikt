package examples.polymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class PolymorphicTypeOneAnotherRef(
  @param:JsonProperty("first_name")
  @get:JsonProperty("first_name")
  @get:NotNull
  override val firstName: String,
  @param:JsonProperty("last_name")
  @get:JsonProperty("last_name")
  @get:NotNull
  override val lastName: String,
  @param:JsonProperty("pets")
  @get:JsonProperty("pets")
  @get:NotNull
  override val pets: List<Pet>,
  @param:JsonProperty("child_one_name")
  @get:JsonProperty("child_one_name")
  public val childOneName: String? = null,
  @get:JsonProperty("generation")
  @get:NotNull
  @param:JsonProperty("generation")
  override val generation: String = "PolymorphicTypeOne",
  @get:JsonIgnore
  public val properties: MutableMap<String, Any?> = mutableMapOf(),
) : PolymorphicSuperType(generation = generation, firstName = firstName, lastName = lastName, pets = pets),
    PolymorphicType {
  @JsonAnyGetter
  public fun `get`(): Map<String, Any?> = properties

  @JsonAnySetter
  public fun `set`(name: String, `value`: Any?) {
    properties[name] = value
  }
}
