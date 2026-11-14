package examples.singletonHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class Holder(
  @param:JsonProperty("direct")
  @get:JsonProperty("direct")
  @get:Valid
  public val direct: MiddleContract? = null,
  @param:JsonProperty("directValues")
  @get:JsonProperty("directValues")
  @get:Size(min = 1)
  public val directValues: List<@Valid MiddleContract>? = null,
  @param:JsonProperty("directMap")
  @get:JsonProperty("directMap")
  public val directMap: Map<String, @Valid MiddleContract?>? = null,
  @param:JsonProperty("bag")
  @get:JsonProperty("bag")
  @get:Valid
  public val bag: MiddleBag? = null,
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
