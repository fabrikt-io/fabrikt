package examples.externalReferences.aggressive.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.`annotation`.JsonValue
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class ConflictingSchemaName(
    @param:JsonProperty("conflictsInMainSpecFile")
    @get:JsonProperty("conflictsInMainSpecFile")
    @get:NotNull
    public val conflictsInMainSpecFile: String,
)

public data class ConflictingSchemaNameExtra(
    @param:JsonProperty("conflictsInExternalSpecFile")
    @get:JsonProperty("conflictsInExternalSpecFile")
    @get:NotNull
    public val conflictsInExternalSpecFile: String,
)

public data class ContainingExternalReference(
    @param:JsonProperty("some-external-reference")
    @get:JsonProperty("some-external-reference")
    @get:Valid
    public val someExternalReference: ExternalObject? = null,
    @param:JsonProperty("inlineObject")
    @get:JsonProperty("inlineObject")
    @get:Valid
    public val inlineObject: FirstInlineObject? = null,
)

public data class ExternalObject(
    @param:JsonProperty("another")
    @get:JsonProperty("another")
    @get:Valid
    public val another: ExternalObjectTwo? = null,
    @param:JsonProperty("one_of")
    @get:JsonProperty("one_of")
    @get:Valid
    public val oneOf: ParentOneOf? = null,
    @param:JsonProperty("anotherExternal")
    @get:JsonProperty("anotherExternal")
    @get:Valid
    public val anotherExternal: ReferencedFromOtherExternalFile? = null,
    @param:JsonProperty("conflicting")
    @get:JsonProperty("conflicting")
    @get:Valid
    public val conflicting: ConflictingSchemaNameExtra? = null,
)

public data class ExternalObjectFour(
    @param:JsonProperty("blah")
    @get:JsonProperty("blah")
    public val blah: String? = null,
)

public data class ExternalObjectThree(
    @param:JsonProperty("enum")
    @get:JsonProperty("enum")
    @get:NotNull
    public val `enum`: ExternalObjectThreeEnum,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    @get:NotNull
    public val description: String,
)

public enum class ExternalObjectThreeEnum(
    @JsonValue
    public val `value`: String,
) {
    ONE("one"),
    TWO("two"),
    THREE("three"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, ExternalObjectThreeEnum> =
            entries.associateBy(ExternalObjectThreeEnum::value)

        public fun fromValue(`value`: String): ExternalObjectThreeEnum? = mapping[value]
    }
}

public data class ExternalObjectTwo(
    @param:JsonProperty("list-others")
    @get:JsonProperty("list-others")
    @get:Valid
    public val listOthers: List<ExternalObjectThree>? = null,
    @get:JsonIgnore
    public val properties: MutableMap<String, Map<String, ExternalObjectFour?>?> = mutableMapOf(),
) {
    @JsonAnyGetter
    public fun `get`(): Map<String, Map<String, ExternalObjectFour?>?> = properties

    @JsonAnySetter
    public fun `set`(
        name: String,
        `value`: Map<String, ExternalObjectFour?>?,
    ) {
        properties[name] = value
    }
}

public enum class ExternalParameter(
    @JsonValue
    public val `value`: String,
) {
    ONE("ONE"),
    TWO("TWO"),
    THREE("THREE"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, ExternalParameter> =
            entries.associateBy(ExternalParameter::value)

        public fun fromValue(`value`: String): ExternalParameter? = mapping[value]
    }
}

public data class FirstInlineObject(
    @param:JsonProperty("generation")
    @get:JsonProperty("generation")
    @get:Valid
    public val generation: FirstInlineObjectGeneration? = null,
)

public data class FirstInlineObjectCallHome(
    @param:JsonProperty("url")
    @get:JsonProperty("url")
    @get:NotNull
    public val url: String,
)

public data class FirstInlineObjectDatabaseView(
    @param:JsonProperty("view_name")
    @get:JsonProperty("view_name")
    @get:NotNull
    public val viewName: String,
)

public data class FirstInlineObjectGeneration(
    @param:JsonProperty("call_home")
    @get:JsonProperty("call_home")
    @get:Valid
    public val callHome: FirstInlineObjectCallHome? = null,
    @param:JsonProperty("database_view")
    @get:JsonProperty("database_view")
    @get:Valid
    public val databaseView: FirstInlineObjectDatabaseView? = null,
    @param:JsonProperty("direct")
    @get:JsonProperty("direct")
    public val direct: String? = null,
    @param:JsonProperty("externalInlineObject")
    @get:JsonProperty("externalInlineObject")
    @get:Valid
    public val externalInlineObject: SecondInlineObject? = null,
)

public data class OneOfOne(
    @param:JsonProperty("oneOfOne")
    @get:JsonProperty("oneOfOne")
    public val oneOfOne: String? = null,
    @get:JsonProperty("discriminator")
    @get:NotNull
    @param:JsonProperty("discriminator")
    override val discriminator: String = "OneOfOne",
) : ParentOneOf()

public data class OneOfTwo(
    @param:JsonProperty("oneOfTwo")
    @get:JsonProperty("oneOfTwo")
    public val oneOfTwo: String? = null,
    @get:JsonProperty("discriminator")
    @get:NotNull
    @param:JsonProperty("discriminator")
    override val discriminator: String = "OneOfTwo",
) : ParentOneOf()

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "discriminator",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OneOfOne::class, name = "OneOfOne"),
    JsonSubTypes.Type(
        value =
            OneOfTwo::class,
        name = "OneOfTwo",
    ),
)
public sealed class ParentOneOf {
    public abstract val discriminator: String
}

public data class ReferencedFromOtherExternalFile(
    @param:JsonProperty("another")
    @get:JsonProperty("another")
    public val another: String? = null,
)

public data class SecondInlineObject(
    @param:JsonProperty("generation")
    @get:JsonProperty("generation")
    @get:Valid
    public val generation: SecondInlineObjectGeneration? = null,
)

public data class SecondInlineObjectCallHome(
    @param:JsonProperty("url")
    @get:JsonProperty("url")
    @get:NotNull
    public val url: String,
)

public data class SecondInlineObjectDatabaseView(
    @param:JsonProperty("view_name")
    @get:JsonProperty("view_name")
    @get:NotNull
    public val viewName: String,
)

public data class SecondInlineObjectGeneration(
    @param:JsonProperty("call_home")
    @get:JsonProperty("call_home")
    @get:Valid
    public val callHome: SecondInlineObjectCallHome? = null,
    @param:JsonProperty("database_view")
    @get:JsonProperty("database_view")
    @get:Valid
    public val databaseView: SecondInlineObjectDatabaseView? = null,
    @param:JsonProperty("direct")
    @get:JsonProperty("direct")
    public val direct: String? = null,
)

public data class UnreferencedExternalObjectFive(
    @param:JsonProperty("blah")
    @get:JsonProperty("blah")
    public val blah: String? = null,
)
