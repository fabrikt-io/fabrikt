package examples.polymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class PolymorphicTypeTwo(
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
  @param:JsonProperty("some_integer_propery")
  @get:JsonProperty("some_integer_propery")
  public val someIntegerPropery: Int? = null,
  @param:JsonProperty("child_two_age")
  @get:JsonProperty("child_two_age")
  public val childTwoAge: Int? = null,
  @get:JsonProperty("generation")
  @get:NotNull
  @param:JsonProperty("generation")
  override val generation: String = "polymorphic_type_two",
  @get:JsonIgnore
  public val properties: MutableMap<String, String?> = mutableMapOf(),
) : PolymorphicSuperType(generation = generation, firstName = firstName, lastName = lastName, pets = pets),
    PolymorphicType {
  @JsonAnyGetter
  public fun `get`(): Map<String, String?> = properties

  @JsonAnySetter
  public fun `set`(name: String, `value`: String?) {
    properties[name] = value
  }
}
