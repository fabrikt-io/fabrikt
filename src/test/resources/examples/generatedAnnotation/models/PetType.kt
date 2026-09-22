package com.example.models

import com.fasterxml.jackson.`annotation`.JsonValue
import javax.`annotation`.processing.Generated
import kotlin.String
import kotlin.collections.Map

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public enum class PetType(
    @JsonValue
    public val `value`: String,
) {
    LEGGED("legged"),
    WINGED("winged"),
    FINNED("finned"),
    ;

    override fun toString(): String = value

    @Generated(
        value = ["io.fabrikt.cli.CodeGen"],
        date = "2001-07-04T19:08:56.235Z",
        comments = "Generated with Fabrikt v27.0.1",
    )
    public companion object {
        private val mapping: Map<String, PetType> = entries.associateBy(PetType::value)

        public fun fromValue(`value`: String): PetType? = mapping[value]
    }
}
