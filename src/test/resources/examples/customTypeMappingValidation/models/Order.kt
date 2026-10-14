package examples.customTypeMappingValidation.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import examples.customTypeMappingValidation.MerchantId
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlin.String
import kotlin.collections.List

public data class Order(
  @param:JsonProperty("merchant_id")
  @get:JsonProperty("merchant_id")
  @get:NotNull
  public val merchantId: MerchantId,
  @param:JsonProperty("merchant_ids")
  @get:JsonProperty("merchant_ids")
  @get:NotNull
  @get:Size(
    min = 1,
    max = 3,
  )
  public val merchantIds: List<MerchantId>,
  @param:JsonProperty("reference")
  @get:JsonProperty("reference")
  @get:NotNull
  @get:Pattern(regexp = "^[A-Z0-9-]+${'$'}")
  @get:Size(
    min = 1,
    max = 16,
  )
  public val reference: String,
  @param:JsonProperty("slug")
  @get:JsonProperty("slug")
  @get:Pattern(regexp = "^[a-z0-9-]+${'$'}")
  @get:Size(max = 32)
  public val slug: String? = null,
)
