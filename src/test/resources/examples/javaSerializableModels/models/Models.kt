package examples.javaSerializableModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.`annotation`.JsonValue
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.io.Serializable
import java.time.OffsetDateTime
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "model_type",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = FirstModel::class,
        name =
            "first_model",
    ),
    JsonSubTypes.Type(
        value = SecondModel::class,
        name =
            "second_model",
    ),
    JsonSubTypes.Type(value = ThirdModel::class, name = "third_model"),
)
public sealed class Content(
    /**
     * The unique resource id
     */
    public open val id: String? = null,
    /**
     * The attribute 1
     */
    public open val firstAttr: OffsetDateTime? = null,
    /**
     * The attribute 2
     */
    public open val secondAttr: String? = null,
    /**
     * Enum types for attribute 3
     */
    public open val thirdAttr: ContentThirdAttr? = null,
    /**
     * Etag value to be used in conjunction with If-Match headers for optimistic locking purposes
     */
    public open val etag: String? = null,
) {
    /**
     * The model discrimination type
     */
    public abstract val modelType: ContentModelType
}

/**
 * The model discrimination type
 */
public enum class ContentModelType(
    @JsonValue
    public val `value`: String,
) {
    FIRST_MODEL("first_model"),
    SECOND_MODEL("second_model"),
    THIRD_MODEL("third_model"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, ContentModelType> =
            entries.associateBy(ContentModelType::value)

        public fun fromValue(`value`: String): ContentModelType? = mapping[value]
    }
}

/**
 * Enum types for attribute 3
 */
public enum class ContentThirdAttr(
    @JsonValue
    public val `value`: String,
) {
    ENUM_TYPE_1("enum_type_1"),
    ENUM_TYPE_2("enum_type_2"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, ContentThirdAttr> =
            entries.associateBy(ContentThirdAttr::value)

        public fun fromValue(`value`: String): ContentThirdAttr? = mapping[value]
    }
}

public data class FirstModel(
    /**
     * The unique resource id
     */
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    override val id: String? = null,
    /**
     * The attribute 1
     */
    @param:JsonProperty("first_attr")
    @get:JsonProperty("first_attr")
    override val firstAttr: OffsetDateTime? = null,
    /**
     * The attribute 2
     */
    @param:JsonProperty("second_attr")
    @get:JsonProperty("second_attr")
    override val secondAttr: String? = null,
    /**
     * Enum types for attribute 3
     */
    @param:JsonProperty("third_attr")
    @get:JsonProperty("third_attr")
    override val thirdAttr: ContentThirdAttr? = null,
    /**
     * Etag value to be used in conjunction with If-Match headers for optimistic locking purposes
     */
    @param:JsonProperty("etag")
    @get:JsonProperty("etag")
    override val etag: String? = null,
    /**
     * The attribute 1 for model 1
     */
    @param:JsonProperty("extra_first_attr")
    @get:JsonProperty("extra_first_attr")
    public val extraFirstAttr: List<String>? = null,
    /**
     * The model discrimination type
     */
    @get:JsonProperty("model_type")
    @get:NotNull
    @param:JsonProperty("model_type")
    override val modelType: ContentModelType = ContentModelType.FIRST_MODEL,
) : Content(id, firstAttr, secondAttr, thirdAttr, etag),
    Serializable

public data class QueryResult(
    @param:JsonProperty("items")
    @get:JsonProperty("items")
    @get:NotNull
    @get:Size(min = 0)
    @get:Valid
    public val items: List<Content>,
) : Serializable

public data class SecondModel(
    /**
     * The unique resource id
     */
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    override val id: String? = null,
    /**
     * The attribute 1
     */
    @param:JsonProperty("first_attr")
    @get:JsonProperty("first_attr")
    override val firstAttr: OffsetDateTime? = null,
    /**
     * The attribute 2
     */
    @param:JsonProperty("second_attr")
    @get:JsonProperty("second_attr")
    override val secondAttr: String? = null,
    /**
     * Enum types for attribute 3
     */
    @param:JsonProperty("third_attr")
    @get:JsonProperty("third_attr")
    override val thirdAttr: ContentThirdAttr? = null,
    /**
     * Etag value to be used in conjunction with If-Match headers for optimistic locking purposes
     */
    @param:JsonProperty("etag")
    @get:JsonProperty("etag")
    override val etag: String? = null,
    /**
     * The attribute 1 for model 2
     */
    @param:JsonProperty("extra_first_attr")
    @get:JsonProperty("extra_first_attr")
    public val extraFirstAttr: String? = null,
    /**
     * The attribute 2 for model 2
     */
    @param:JsonProperty("extra_second_attr")
    @get:JsonProperty("extra_second_attr")
    public val extraSecondAttr: Boolean? = null,
    /**
     * The model discrimination type
     */
    @get:JsonProperty("model_type")
    @get:NotNull
    @param:JsonProperty("model_type")
    override val modelType: ContentModelType = ContentModelType.SECOND_MODEL,
) : Content(id, firstAttr, secondAttr, thirdAttr, etag),
    Serializable

public data class ThirdModel(
    /**
     * The unique resource id
     */
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    override val id: String? = null,
    /**
     * The attribute 1
     */
    @param:JsonProperty("first_attr")
    @get:JsonProperty("first_attr")
    override val firstAttr: OffsetDateTime? = null,
    /**
     * The attribute 2
     */
    @param:JsonProperty("second_attr")
    @get:JsonProperty("second_attr")
    override val secondAttr: String? = null,
    /**
     * Enum types for attribute 3
     */
    @param:JsonProperty("third_attr")
    @get:JsonProperty("third_attr")
    override val thirdAttr: ContentThirdAttr? = null,
    /**
     * Etag value to be used in conjunction with If-Match headers for optimistic locking purposes
     */
    @param:JsonProperty("etag")
    @get:JsonProperty("etag")
    override val etag: String? = null,
    /**
     * The attribute 1 for model 3
     */
    @param:JsonProperty("extra_first_attr")
    @get:JsonProperty("extra_first_attr")
    public val extraFirstAttr: OffsetDateTime? = null,
    /**
     * The attribute 2 for model 3
     */
    @param:JsonProperty("extra_second_attr")
    @get:JsonProperty("extra_second_attr")
    public val extraSecondAttr: Int? = null,
    /**
     * The model discrimination type
     */
    @get:JsonProperty("model_type")
    @get:NotNull
    @param:JsonProperty("model_type")
    override val modelType: ContentModelType = ContentModelType.THIRD_MODEL,
) : Content(id, firstAttr, secondAttr, thirdAttr, etag),
    Serializable
