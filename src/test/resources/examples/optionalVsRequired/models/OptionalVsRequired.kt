package examples.optionalVsRequired.models

import com.fasterxml.jackson.`annotation`.JsonInclude
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import java.util.UUID
import kotlin.Any
import kotlin.String
import kotlin.collections.Map

public data class OptionalVsRequired(
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  public val name: String,
  @param:JsonProperty("gender")
  @get:JsonProperty("gender")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val gender: UUID? = null,
  @param:JsonProperty("requiredNullableString")
  @get:JsonProperty("requiredNullableString")
  @param:JsonInclude(JsonInclude.Include.ALWAYS)
  public val requiredNullableString: String?,
  @param:JsonProperty("requiredNullableUntypedObject")
  @get:JsonProperty("requiredNullableUntypedObject")
  @param:JsonInclude(JsonInclude.Include.ALWAYS)
  public val requiredNullableUntypedObject: Map<String, Any?>?,
)
