package examples.externalPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ConcreteImplOne(
  @param:JsonProperty("some_prop")
  @get:JsonProperty("some_prop")
  public val someProp: String? = null,
  @get:JsonProperty("some_enum")
  @get:NotNull
  @param:JsonProperty("some_enum")
  override val someEnum: EnumDiscriminator = EnumDiscriminator.OBJ_ONE_ONLY,
) : PolymorphicEnumDiscriminator()
