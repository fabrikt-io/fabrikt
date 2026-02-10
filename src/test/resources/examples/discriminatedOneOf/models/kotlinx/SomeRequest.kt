package examples.discriminatedOneOf.models

import java.math.BigDecimal
import javax.validation.Valid
import kotlin.collections.List
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class SomeRequest(
  @Contextual
  @SerialName("id")
  public val id: BigDecimal? = null,
  @SerialName("events")
  @get:Valid
  public val events: List<Test>? = null,
)
