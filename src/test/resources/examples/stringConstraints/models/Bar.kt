package examples.stringConstraints.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import examples.stringConstraints.FooId
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime
import kotlin.String
import kotlin.collections.List

public data class Bar(
  @param:JsonProperty("foo_id")
  @get:JsonProperty("foo_id")
  @get:NotNull
  public val fooId: FooId,
  @param:JsonProperty("foo_ids")
  @get:JsonProperty("foo_ids")
  @get:NotNull
  @get:Size(
    min = 1,
    max = 3,
  )
  public val fooIds: List<FooId>,
  @param:JsonProperty("reference")
  @get:JsonProperty("reference")
  @get:NotNull
  @get:Pattern(regexp = "[\\s\\S]*(?:^[A-Z0-9-]+\\z)[\\s\\S]*")
  @get:Size(
    min = 1,
    max = 16,
  )
  public val reference: String,
  @param:JsonProperty("created_at")
  @get:JsonProperty("created_at")
  public val createdAt: OffsetDateTime? = null,
)
