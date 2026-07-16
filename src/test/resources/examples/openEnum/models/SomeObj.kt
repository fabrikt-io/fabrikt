package examples.openEnum.models

import com.fasterxml.jackson.`annotation`.JsonProperty

public data class SomeObj(
  @param:JsonProperty("open_enum")
  @get:JsonProperty("open_enum")
  public val openEnum: OpenEnum? = null,
  @param:JsonProperty("close_enum")
  @get:JsonProperty("close_enum")
  public val closeEnum: CloseEnum? = null,
)
