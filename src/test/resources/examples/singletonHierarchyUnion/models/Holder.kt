package examples.singletonHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import kotlin.collections.List

public data class Holder(
  @param:JsonProperty("single")
  @get:JsonProperty("single")
  @get:Valid
  public val single: Child? = null,
  @param:JsonProperty("values")
  @get:JsonProperty("values")
  @get:Size(min = 1)
  public val values: List<@Valid Child>? = null,
  @param:JsonProperty("nullableValues")
  @get:JsonProperty("nullableValues")
  public val nullableValues: List<@Valid Child?>? = null,
  @param:JsonProperty("parent")
  @get:JsonProperty("parent")
  @get:Valid
  public val parent: Base? = null,
  @param:JsonProperty("middle")
  @get:JsonProperty("middle")
  @get:Valid
  public val middle: Base? = null,
  @param:JsonProperty("subset")
  @get:JsonProperty("subset")
  @get:Valid
  public val subset: Base? = null,
  @param:JsonProperty("family")
  @get:JsonProperty("family")
  @get:Valid
  public val family: Base? = null,
  @param:JsonProperty("childrenOnly")
  @get:JsonProperty("childrenOnly")
  @get:Valid
  public val childrenOnly: Base? = null,
  @param:JsonProperty("preciseSubset")
  @get:JsonProperty("preciseSubset")
  @get:Valid
  public val preciseSubset: HolderPreciseSubset? = null,
)
