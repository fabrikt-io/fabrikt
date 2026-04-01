package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class MapHolderDto(
  @param:JsonProperty("wild_card")
  @get:JsonProperty("wild_card")
  public val wildCard: Map<String, Any?>? = null,
  @param:JsonProperty("string_map")
  @get:JsonProperty("string_map")
  public val stringMap: Map<String, String?>? = null,
  @param:JsonProperty("typed_object_map")
  @get:JsonProperty("typed_object_map")
  @get:Valid
  public val typedObjectMap: Map<String, TypedObjectMapValueDto?>? = null,
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
  public val inlinedTypedObjectMap: Map<String, InlinedTypedObjectMapValueDto?>? = null,
  @param:JsonProperty("complex_object_with_untyped_map")
  @get:JsonProperty("complex_object_with_untyped_map")
  @get:Valid
  public val complexObjectWithUntypedMap: ComplexObjectWithUntypedMapDto? = null,
  @param:JsonProperty("complex_object_with_typed_map")
  @get:JsonProperty("complex_object_with_typed_map")
  @get:Valid
  public val complexObjectWithTypedMap: ComplexObjectWithTypedMapDto? = null,
  @param:JsonProperty("inlined_complex_object_with_untyped_map")
  @get:JsonProperty("inlined_complex_object_with_untyped_map")
  @get:Valid
  public val inlinedComplexObjectWithUntypedMap: MapHolderDtoInlinedComplexObjectWithUntypedMapDto?
      = null,
  @param:JsonProperty("inlined_complex_object_with_typed_map")
  @get:JsonProperty("inlined_complex_object_with_typed_map")
  @get:Valid
  public val inlinedComplexObjectWithTypedMap: MapHolderDtoInlinedComplexObjectWithTypedMapDto? =
      null,
  @get:JsonIgnore
  public val properties: MutableMap<String, Map<String, ExternalObjectFourDto?>?> = mutableMapOf(),
) {
  @JsonAnyGetter
  public fun `get`(): Map<String, Map<String, ExternalObjectFourDto?>?> = properties

  @JsonAnySetter
  public fun `set`(name: String, `value`: Map<String, ExternalObjectFourDto?>?) {
    properties[name] = value
  }
}
