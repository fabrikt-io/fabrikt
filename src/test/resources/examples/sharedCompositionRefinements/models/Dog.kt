package examples.sharedCompositionRefinements.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String

public data class Dog(
  @param:JsonProperty(
    "age",
    required = true,
  )
  @get:JsonProperty("age")
  @get:NotNull
  @get:DecimalMin(
    value = "0",
    inclusive = true,
  )
  public val age: Int,
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 2)
  override val id: String,
) : Pet(id),
    PetComposite {
  @get:JsonProperty(
    value = "kind",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val kind: String
    get() = "dog"
}
