package examples.discriminatedOneOf.models

import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.NotNull
import kotlin.collections.List
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class SomeRequest(
  @Contextual
  @SerialName("id")
  @get:NotNull
  public val id: BigDecimal,
  @SerialName("events")
  @get:Valid
  public val events: List<Test>? = null,
)
