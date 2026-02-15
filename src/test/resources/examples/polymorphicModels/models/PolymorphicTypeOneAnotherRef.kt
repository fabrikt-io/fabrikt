package examples.polymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List

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
  @get:Valid
  override val pets: List<Pet>,
  @param:JsonProperty("child_one_name")
  @get:JsonProperty("child_one_name")
  public val childOneName: String? = null,
  @get:JsonProperty("generation")
  @get:NotNull
  @param:JsonProperty("generation")
  override val generation: String = "PolymorphicTypeOne",
) : PolymorphicSuperType(firstName, lastName, pets), PolymorphicType
