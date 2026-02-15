package examples.enumPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List

public data class DiscriminatedChild1(
  @param:JsonProperty("inline_obj")
  @get:JsonProperty("inline_obj")
  @get:Valid
  override val inlineObj: ChildDefinitionInlineObj? = null,
  @param:JsonProperty("inline_array")
  @get:JsonProperty("inline_array")
  @get:Valid
  override val inlineArray: List<ChildDefinitionInlineArray>? = null,
  @param:JsonProperty("inline_enum")
  @get:JsonProperty("inline_enum")
  override val inlineEnum: ChildDefinitionInlineEnum? = null,
  @param:JsonProperty("some_prop")
  @get:JsonProperty("some_prop")
  public val someProp: String? = null,
  @get:JsonProperty("some_enum")
  @get:NotNull
  @param:JsonProperty("some_enum")
  override val someEnum: ChildDiscriminator = ChildDiscriminator.OBJ_ONE_ONLY,
) : ChildDefinition(inlineObj, inlineArray, inlineEnum)
