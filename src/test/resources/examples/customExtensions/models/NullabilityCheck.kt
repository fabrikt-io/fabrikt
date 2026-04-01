package examples.customExtensions.models

import com.fasterxml.jackson.`annotation`.JsonInclude
import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String
import org.openapitools.jackson.nullable.JsonNullable

public data class NullabilityCheck(
  @param:JsonProperty("not-null-no-default-not-required")
  @get:JsonProperty("not-null-no-default-not-required")
  public val notNullNoDefaultNotRequired: JsonNullable<String> = JsonNullable.undefined(),
  @param:JsonProperty("nullable-no-default-not-required")
  @get:JsonProperty("nullable-no-default-not-required")
  public val nullableNoDefaultNotRequired: JsonNullable<String?> = JsonNullable.undefined(),
  @param:JsonProperty("not-null-with-default-not-required")
  @get:JsonProperty("not-null-with-default-not-required")
  @get:NotNull
  public val notNullWithDefaultNotRequired: JsonNullable<String> = JsonNullable.of(""),
  @param:JsonProperty("nullable-with-default-not-required")
  @get:JsonProperty("nullable-with-default-not-required")
  public val nullableWithDefaultNotRequired: JsonNullable<String?> = JsonNullable.of(""),
  @param:JsonProperty("not-null-no-default-required")
  @get:JsonProperty("not-null-no-default-required")
  @get:NotNull
  public val notNullNoDefaultRequired: String,
  @param:JsonProperty("nullable-no-default-required")
  @get:JsonProperty("nullable-no-default-required")
  @param:JsonInclude(JsonInclude.Include.ALWAYS)
  public val nullableNoDefaultRequired: String?,
  @param:JsonProperty("not-null-with-default-required")
  @get:JsonProperty("not-null-with-default-required")
  @get:NotNull
  public val notNullWithDefaultRequired: String,
  @param:JsonProperty("nullable-with-default-required")
  @get:JsonProperty("nullable-with-default-required")
  @param:JsonInclude(JsonInclude.Include.ALWAYS)
  public val nullableWithDefaultRequired: String?,
)
