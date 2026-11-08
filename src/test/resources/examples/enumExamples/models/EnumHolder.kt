package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.collections.List

public data class EnumHolder(
  @param:JsonProperty("array_of_enums")
  @get:JsonProperty("array_of_enums")
  public val arrayOfEnums: List<EnumHolderArrayOfEnums>? = null,
  @param:JsonProperty("inlined_enum")
  @get:JsonProperty("inlined_enum")
  public val inlinedEnum: EnumHolderInlinedEnum? = null,
  @param:JsonProperty("inlined_extensible_enum")
  @get:JsonProperty("inlined_extensible_enum")
  public val inlinedExtensibleEnum: EnumHolderInlinedExtensibleEnum? = null,
  @param:JsonProperty("enum_ref")
  @get:JsonProperty("enum_ref")
  public val enumRef: EnumObject? = null,
  @param:JsonProperty("extensible_enum_ref")
  @get:JsonProperty("extensible_enum_ref")
  public val extensibleEnumRef: ExtensibleEnumObject? = null,
  @param:JsonProperty("list_enums")
  @get:JsonProperty("list_enums")
  public val listEnums: List<ContentType>? = null,
  @param:JsonProperty("nullable_ref")
  @get:JsonProperty("nullable_ref")
  public val nullableRef: NullableEnum? = null,
  @param:JsonProperty("extensible_enum_union")
  @get:JsonProperty("extensible_enum_union")
  public val extensibleEnumUnion: ExtensibleEnumUnion? = null,
)
