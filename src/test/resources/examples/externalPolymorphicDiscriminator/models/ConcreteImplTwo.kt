package examples.externalPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ConcreteImplTwo(
  @get:JsonProperty("some_enum")
  @get:NotNull
  override val someEnum: EnumDiscriminator,
  @param:JsonProperty("some_prop")
  @get:JsonProperty("some_prop")
  public val someProp: String? = null,
) : PolymorphicEnumDiscriminator()
