package examples.defaultValues.models

import com.fasterxml.jackson.`annotation`.JsonInclude
import com.fasterxml.jackson.`annotation`.JsonProperty
import java.math.BigDecimal
import java.net.URI
import java.util.Base64
import javax.validation.Valid
import javax.validation.constraints.NotNull
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.String
import kotlin.collections.List

public data class PersonWithDefaults(
  @param:JsonProperty("required_so_default_ignored")
  @get:JsonProperty("required_so_default_ignored")
  @get:NotNull
  public val requiredSoDefaultIgnored: String,
  @param:JsonProperty("integer_default")
  @get:JsonProperty("integer_default")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val integerDefault: Int = 18,
  @param:JsonProperty("decimal_default")
  @get:JsonProperty("decimal_default")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val decimalDefault: BigDecimal = BigDecimal(0.1),
  @param:JsonProperty("enum_default")
  @get:JsonProperty("enum_default")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val enumDefault: PersonWithDefaultsEnumDefault = PersonWithDefaultsEnumDefault.TALL,
  @param:JsonProperty("enum_quoted_default")
  @get:JsonProperty("enum_quoted_default")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val enumQuotedDefault: PersonWithDefaultsEnumQuotedDefault =
      PersonWithDefaultsEnumQuotedDefault.`2X`,
  @param:JsonProperty("boolean_default")
  @get:JsonProperty("boolean_default")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val booleanDefault: Boolean = true,
  @param:JsonProperty("string_phrase")
  @get:JsonProperty("string_phrase")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val stringPhrase: String = "Cowabunga Dude",
  /**
   * An absolute URI that identifies the problem type.  When dereferenced,
   * it SHOULD provide human-readable documentation for the problem type
   * (e.g., using HTML).
   */
  @param:JsonProperty("uri_type")
  @get:JsonProperty("uri_type")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val uriType: URI = URI("about:blank"),
  @param:JsonProperty("byte_type")
  @get:JsonProperty("byte_type")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val byteType: ByteArray = Base64.getDecoder().decode("U3dhZ2dlciByb2Nrcw=="),
  @param:JsonProperty("ignored_array_default")
  @get:JsonProperty("ignored_array_default")
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val ignoredArrayDefault: List<String>? = null,
  @param:JsonProperty("ignored_object_default")
  @get:JsonProperty("ignored_object_default")
  @get:Valid
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val ignoredObjectDefault: PersonWithDefaultsIgnoredObjectDefault? = null,
  @param:JsonProperty("float_with_default_integer")
  @get:JsonProperty("float_with_default_integer")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val floatWithDefaultInteger: Float = 0f,
  @param:JsonProperty("double_with_default_integer")
  @get:JsonProperty("double_with_default_integer")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val doubleWithDefaultInteger: Double = 0.0,
  @param:JsonProperty("float_with_default_float")
  @get:JsonProperty("float_with_default_float")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val floatWithDefaultFloat: Float = 0.1f,
  @param:JsonProperty("double_with_default_float")
  @get:JsonProperty("double_with_default_float")
  @get:NotNull
  @param:JsonInclude(JsonInclude.Include.NON_NULL)
  public val doubleWithDefaultFloat: Double = 0.1,
)
