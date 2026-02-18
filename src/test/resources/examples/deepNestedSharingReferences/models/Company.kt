package examples.deepNestedSharingReferences.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class Company(
  @param:JsonProperty("owner")
  @get:JsonProperty("owner")
  @get:Valid
  public val owner: Person? = null,
  @param:JsonProperty("employee")
  @get:JsonProperty("employee")
  @get:Valid
  public val employee: Person? = null,
)
