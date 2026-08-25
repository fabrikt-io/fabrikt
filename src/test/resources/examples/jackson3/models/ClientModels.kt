package examples.jackson3.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class PendingWidget(
    @param:JsonProperty("status")
    @get:JsonProperty("status")
    @get:NotNull
    public val status: String,
)

public data class Widget(
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    @get:NotNull
    public val id: String,
)
