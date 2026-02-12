package examples.faultTolerantEnums.models

import com.fasterxml.jackson.`annotation`.JsonProperty

public data class EnumContainer(
  @param:JsonProperty("simple_enum")
  @get:JsonProperty("simple_enum")
  public val simpleEnum: SimpleEnum? = null,
  @param:JsonProperty("extensible_enum")
  @get:JsonProperty("extensible_enum")
  public val extensibleEnum: ExtensibleEnum? = null,
  @param:JsonProperty("inlined_enum")
  @get:JsonProperty("inlined_enum")
  public val inlinedEnum: EnumContainerInlinedEnum? = null,
)
