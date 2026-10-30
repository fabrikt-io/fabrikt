package examples.polymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.util.Arrays
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "generation",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = PolymorphicTypeOne::class, name = "PolymorphicTypeOne"),JsonSubTypes.Type(value = PolymorphicTypeTwo::class, name = "polymorphic_type_two"),JsonSubTypes.Type(value = PolymorphicSuperType::class, name = "PolymorphicSuperType"))
public open class PolymorphicSuperType(
  @param:JsonProperty("first_name")
  @get:JsonProperty("first_name")
  @get:NotNull
  public open val firstName: String,
  @param:JsonProperty("last_name")
  @get:JsonProperty("last_name")
  @get:NotNull
  public open val lastName: String,
  @param:JsonProperty("pets")
  @get:JsonProperty("pets")
  @get:NotNull
  public open val pets: List<@Valid Pet>,
  @param:JsonProperty("generation")
  @get:JsonProperty("generation")
  @get:NotNull
  public open val generation: String = "PolymorphicSuperType",
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as PolymorphicSuperType
    return Objects.deepEquals(this.generation, other.generation) && Objects.deepEquals(this.firstName, other.firstName) && Objects.deepEquals(this.lastName, other.lastName) && Objects.deepEquals(this.pets, other.pets)
  }

  override fun hashCode(): Int = Arrays.deepHashCode(arrayOf<Any?>(this.generation, this.firstName, this.lastName, this.pets))

  override fun toString(): String = "PolymorphicSuperType(" + "generation=" + this.generation + ", firstName=" + this.firstName + ", lastName=" + this.lastName + ", pets=" + this.pets + ")"
}
