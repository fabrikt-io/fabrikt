package examples.aggregatedEnum.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonValue
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public enum class SomeEnumDto(
    @JsonValue
    public val `value`: String,
) {
    FOO("foo"),
    BAR("bar"),
    BAZ("baz"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, SomeEnumDto> = entries.associateBy(SomeEnumDto::value)

        public fun fromValue(`value`: String): SomeEnumDto? = mapping[value]
    }
}

public data class SomeObjectDto(
    @param:JsonProperty("direct")
    @get:JsonProperty("direct")
    public val direct: SomeEnumDto? = null,
    /**
     * A wrapped enum with its own default.
     */
    @param:JsonProperty("withDefault")
    @get:JsonProperty("withDefault")
    @get:NotNull
    public val withDefault: SomeEnumDto = SomeEnumDto.BAZ,
    @param:JsonProperty("withoutDefault")
    @get:JsonProperty("withoutDefault")
    public val withoutDefault: SomeEnumDto? = null,
    @param:JsonProperty("nullable")
    @get:JsonProperty("nullable")
    public val nullable: SomeEnumDto? = SomeEnumDto.BAZ,
    @param:JsonProperty("anyOfDefault")
    @get:JsonProperty("anyOfDefault")
    @get:NotNull
    public val anyOfDefault: SomeEnumDto = SomeEnumDto.BAR,
    @param:JsonProperty("values")
    @get:JsonProperty("values")
    public val values: List<SomeEnumDto>? = null,
)
