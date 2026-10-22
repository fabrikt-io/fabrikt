package examples.polymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List

public data class PolymorphicTypeOne(
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
  override val pets: List<@Valid Pet>,
  @param:JsonProperty("child_one_name")
  @get:JsonProperty("child_one_name")
  public val childOneName: String? = null,
) : PolymorphicSuperType(firstName, lastName, pets),
    PolymorphicType {
  @get:JsonProperty(
    value = "generation",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val generation: String
    get() = "PolymorphicTypeOne"
}
