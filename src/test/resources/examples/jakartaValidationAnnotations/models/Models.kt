package examples.jakartaValidationAnnotations.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class ValidationAnnotations(
    @param:JsonProperty("user_name")
    @get:JsonProperty("user_name")
    @get:NotNull
    @get:Pattern(regexp = "[a-zA-Z]")
    public val userName: String,
    @param:JsonProperty(
        "age",
        required = true,
    )
    @get:JsonProperty("age")
    @get:NotNull
    @get:DecimalMin(
        value = "0",
        inclusive = false,
    )
    @get:DecimalMax(
        value = "100",
        inclusive = true,
    )
    public val age: Int,
    @param:JsonProperty("bio")
    @get:JsonProperty("bio")
    @get:NotNull
    @get:Size(
        min = 20,
        max = 200,
    )
    public val bio: String,
    @param:JsonProperty("friends")
    @get:JsonProperty("friends")
    @get:NotNull
    @get:Size(
        min = 0,
        max = 10,
    )
    @get:Valid
    public val friends: List<ValidationAnnotationsFriends>,
    @param:JsonProperty("address")
    @get:JsonProperty("address")
    @get:Valid
    public val address: ValidationAnnotationsAddress? = null,
    @param:JsonProperty("qualities")
    @get:JsonProperty("qualities")
    @get:Valid
    public val qualities: Map<String, QualitiesValue?>? = null,
)
