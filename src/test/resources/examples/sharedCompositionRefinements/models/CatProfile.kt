package examples.sharedCompositionRefinements.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class CatProfile(
  @param:JsonProperty("pet")
  @get:JsonProperty("pet")
  @get:NotNull
  @get:Valid
  override val pet: Cat,
  @param:JsonProperty("pets")
  @get:JsonProperty("pets")
  @get:NotNull
  @get:Size(
    min = 1,
    max = 3,
  )
  override val pets: List<@Valid Cat>,
  @param:JsonProperty("petsByName")
  @get:JsonProperty("petsByName")
  @get:NotNull
  override val petsByName: Map<String, @Valid Cat?>,
  @param:JsonProperty("optionalPet")
  @get:JsonProperty("optionalPet")
  @get:NotNull
  @get:Valid
  override val optionalPet: Cat,
  @param:JsonProperty("note")
  @get:JsonProperty("note")
  @get:NotNull
  @get:Size(max = 20)
  override val note: String = "untitled",
) : ProfileComposite, CatProfileComposite
