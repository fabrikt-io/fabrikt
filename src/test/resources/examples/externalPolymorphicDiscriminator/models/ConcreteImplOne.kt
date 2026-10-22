package examples.externalPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ConcreteImplOne(
  @param:JsonProperty("some_prop")
  @get:JsonProperty("some_prop")
  public val someProp: String? = null,
) : PolymorphicEnumDiscriminator() {
  @get:JsonProperty(
    value = "some_enum",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val someEnum: EnumDiscriminator
    get() = EnumDiscriminator.OBJ_ONE_ONLY
}
