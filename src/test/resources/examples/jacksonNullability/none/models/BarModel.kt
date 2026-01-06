package examples.jacksonNullability.none.models

import com.fasterxml.jackson.`annotation`.JsonInclude
import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class BarModel(
    @param:JsonProperty("optionalNullable")
    @get:JsonProperty("optionalNullable")
    @param:JsonInclude(JsonInclude.Include.NON_NULL)
    public val optionalNullable: String? = null,
    @param:JsonProperty("requiredNullable")
    @get:JsonProperty("requiredNullable")
    public val requiredNullable: String?,
    @param:JsonProperty("optionalNonNull")
    @get:JsonProperty("optionalNonNull")
    @param:JsonInclude(JsonInclude.Include.NON_NULL)
    public val optionalNonNull: String? = null,
    @param:JsonProperty("requiredNonNull")
    @get:JsonProperty("requiredNonNull")
    @get:NotNull
    public val requiredNonNull: String,
)
