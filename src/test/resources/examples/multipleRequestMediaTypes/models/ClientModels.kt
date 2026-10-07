package examples.multipleRequestMediaTypes.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String

public data class ArrayDetailsRequestItem(
    @param:JsonProperty("label")
    @get:JsonProperty("label")
    @get:NotNull
    @get:Size(min = 1)
    public val label: String,
)

public data class CountRequest(
    @param:JsonProperty(
        "count",
        required = true,
    )
    @get:JsonProperty("count")
    @get:NotNull
    @get:DecimalMin(
        value = "1",
        inclusive = true,
    )
    public val count: Int,
)

public data class DifferentInlineRequestApplicationJson(
    @param:JsonProperty("label")
    @get:JsonProperty("label")
    @get:NotNull
    @get:Size(min = 1)
    public val label: String,
)

public data class DifferentInlineRequestTextJson(
    @param:JsonProperty(
        "count",
        required = true,
    )
    @get:JsonProperty("count")
    @get:NotNull
    @get:DecimalMin(
        value = "1",
        inclusive = true,
    )
    public val count: Int,
)

public data class InlineDetailsRequest(
    @param:JsonProperty("label")
    @get:JsonProperty("label")
    @get:NotNull
    @get:Size(min = 1)
    public val label: String,
)

public data class RequestsDetailsRequest(
    @param:JsonProperty("label")
    @get:JsonProperty("label")
    @get:NotNull
    @get:Size(min = 1)
    public val label: String,
)
