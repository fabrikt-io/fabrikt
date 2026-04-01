package examples.mapExamples.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class MapHolder(
  @param:JsonProperty("wild_card")
  @get:JsonProperty("wild_card")
  public val wildCard: Map<String, Any?>? = null,
  @param:JsonProperty("string_map")
  @get:JsonProperty("string_map")
  public val stringMap: Map<String, String?>? = null,
  @param:JsonProperty("typed_object_map")
  @get:JsonProperty("typed_object_map")
  @get:Valid
  public val typedObjectMap: Map<String, TypedObjectMapValue?>? = null,
  @param:JsonProperty("object_map")
  @get:JsonProperty("object_map")
  public val objectMap: Map<String, Map<String, Any?>?>? = null,
  @param:JsonProperty("inlined_string_map")
  @get:JsonProperty("inlined_string_map")
  public val inlinedStringMap: Map<String, String?>? = null,
  @param:JsonProperty("inlined_object_map")
  @get:JsonProperty("inlined_object_map")
  public val inlinedObjectMap: Map<String, Map<String, Any?>?>? = null,
  @param:JsonProperty("inlined_unknown_map")
  @get:JsonProperty("inlined_unknown_map")
  public val inlinedUnknownMap: Map<String, Any?>? = null,
  @param:JsonProperty("inlined_typed_object_map")
  @get:JsonProperty("inlined_typed_object_map")
  @get:Valid
  public val inlinedTypedObjectMap: Map<String, InlinedTypedObjectMapValue?>? = null,
  @param:JsonProperty("complex_object_with_untyped_map")
  @get:JsonProperty("complex_object_with_untyped_map")
  @get:Valid
  public val complexObjectWithUntypedMap: ComplexObjectWithUntypedMap? = null,
  @param:JsonProperty("complex_object_with_typed_map")
  @get:JsonProperty("complex_object_with_typed_map")
  @get:Valid
  public val complexObjectWithTypedMap: ComplexObjectWithTypedMap? = null,
  @param:JsonProperty("inlined_complex_object_with_untyped_map")
  @get:JsonProperty("inlined_complex_object_with_untyped_map")
  @get:Valid
  public val inlinedComplexObjectWithUntypedMap: MapHolderInlinedComplexObjectWithUntypedMap? =
      null,
  @param:JsonProperty("inlined_complex_object_with_typed_map")
  @get:JsonProperty("inlined_complex_object_with_typed_map")
  @get:Valid
  public val inlinedComplexObjectWithTypedMap: MapHolderInlinedComplexObjectWithTypedMap? = null,
  @param:JsonProperty("contains_polymorphic_map_reference")
  @get:JsonProperty("contains_polymorphic_map_reference")
  @get:Valid
  public val containsPolymorphicMapReference: ContainsReferenceToPolymorphicMap? = null,
  @get:JsonIgnore
  public val properties: MutableMap<String, Map<String, ExternalObjectFour?>?> = mutableMapOf(),
) {
  @JsonAnyGetter
  public fun `get`(): Map<String, Map<String, ExternalObjectFour?>?> = properties

  @JsonAnySetter
  public fun `set`(name: String, `value`: Map<String, ExternalObjectFour?>?) {
    properties[name] = value
  }
}
