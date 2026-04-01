package examples.noValidationAnnotations.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class ValidationAnnotations(
    @param:JsonProperty("user_name")
    @get:JsonProperty("user_name")
    public val userName: String,
    @param:JsonProperty(
        "age",
        required = true,
    )
    @get:JsonProperty("age")
    public val age: Int,
    @param:JsonProperty("bio")
    @get:JsonProperty("bio")
    public val bio: String,
    @param:JsonProperty("friends")
    @get:JsonProperty("friends")
    public val friends: List<ValidationAnnotationsFriends>,
    @param:JsonProperty("address")
    @get:JsonProperty("address")
    public val address: ValidationAnnotationsAddress? = null,
    @param:JsonProperty("qualities")
    @get:JsonProperty("qualities")
    public val qualities: Map<String, QualitiesValue?>? = null,
)
