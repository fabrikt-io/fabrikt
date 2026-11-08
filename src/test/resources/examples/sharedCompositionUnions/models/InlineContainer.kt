package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class InlineContainer(
  @param:JsonProperty("named")
  @get:JsonProperty("named")
  @get:Valid
  public val named: Shared? = null,
  @param:JsonProperty("direct")
  @get:JsonProperty("direct")
  @get:Valid
  public val direct: InlineContainerDirect? = null,
  @param:JsonProperty("alias")
  @get:JsonProperty("alias")
  public val alias: List<@Valid InlineArray>? = null,
  @param:JsonProperty("entries")
  @get:JsonProperty("entries")
  public val entries: List<@Valid InlineContainerEntries>? = null,
  @param:JsonProperty("deduced")
  @get:JsonProperty("deduced")
  public val deduced: List<@Valid InlineContainerDeduced>? = null,
  @param:JsonProperty("matrix")
  @get:JsonProperty("matrix")
  public val matrix: List<List<@Valid InlineContainerMatrix>>? = null,
  @param:JsonProperty("nested")
  @get:JsonProperty("nested")
  @get:Valid
  public val nested: InlineContainerNested? = null,
  @param:JsonProperty("composed")
  @get:JsonProperty("composed")
  @get:Valid
  public val composed: InlineContainerComposed? = null,
  @param:JsonProperty("rows")
  @get:JsonProperty("rows")
  public val rows: List<@Valid InlineContainerRows>? = null,
  @param:JsonProperty("mixed")
  @get:JsonProperty("mixed")
  @get:Valid
  public val mixed: InlineContainerMixed? = null,
  @param:JsonProperty("records")
  @get:JsonProperty("records")
  public val records: Map<String, @Valid RecordsValue?>? = null,
)
