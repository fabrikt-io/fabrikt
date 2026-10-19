package examples.externalPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull

public data class ConcreteImplThree(
  @get:JsonProperty("some_enum")
  @get:NotNull
  @param:JsonProperty("some_enum")
  override val someEnum: EnumDiscriminator = EnumDiscriminator.OBJ_THREE,
) : PolymorphicEnumDiscriminator()
