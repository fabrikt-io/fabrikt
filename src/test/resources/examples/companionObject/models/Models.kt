package examples.companionObject.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.`annotation`.JsonValue
import jakarta.validation.constraints.NotNull
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.Map

public data class Cat(
    @param:JsonProperty(
        "id",
        required = true,
    )
    @get:JsonProperty("id")
    @get:NotNull
    override val id: Long,
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    @get:NotNull
    override val name: String,
    @param:JsonProperty("tag")
    @get:JsonProperty("tag")
    override val tag: String? = null,
    @param:JsonProperty("hunts")
    @get:JsonProperty("hunts")
    public val hunts: Boolean? = null,
    @param:JsonProperty("age")
    @get:JsonProperty("age")
    public val age: Int? = null,
    @get:JsonProperty("petType")
    @get:NotNull
    @param:JsonProperty("petType")
    override val petType: String = "Cat",
) : Pet(id, name, tag) {
    public companion object
}

public data class Dog(
    @param:JsonProperty(
        "id",
        required = true,
    )
    @get:JsonProperty("id")
    @get:NotNull
    override val id: Long,
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    @get:NotNull
    override val name: String,
    @param:JsonProperty("tag")
    @get:JsonProperty("tag")
    override val tag: String? = null,
    @param:JsonProperty("bark")
    @get:JsonProperty("bark")
    public val bark: Boolean? = null,
    @param:JsonProperty("breed")
    @get:JsonProperty("breed")
    public val breed: DogBreed? = null,
    @get:JsonProperty("petType")
    @get:NotNull
    @param:JsonProperty("petType")
    override val petType: String = "Dog",
) : Pet(id, name, tag) {
    public companion object
}

public enum class DogBreed(
    @JsonValue
    public val `value`: String,
) {
    DINGO("Dingo"),
    HUSKY("Husky"),
    RETRIEVER("Retriever"),
    SHEPHERD("Shepherd"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, DogBreed> = entries.associateBy(DogBreed::value)

        public fun fromValue(`value`: String): DogBreed? = mapping[value]
    }
}

public data class Error(
    @param:JsonProperty(
        "code",
        required = true,
    )
    @get:JsonProperty("code")
    @get:NotNull
    public val code: Int,
    @param:JsonProperty("message")
    @get:JsonProperty("message")
    @get:NotNull
    public val message: String,
) {
    public companion object
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "petType",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Cat::class, name = "Cat"),
    JsonSubTypes.Type(
        value =
            Dog::class,
        name = "Dog",
    ),
)
public sealed class Pet(
    public open val id: Long,
    public open val name: String,
    public open val tag: String? = null,
) {
    public abstract val petType: String
}
