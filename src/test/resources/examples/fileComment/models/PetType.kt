//
// This file was generated from an OpenAPI specification by Fabrikt.
// DO NOT EDIT. Any changes will be overwritten the next time the code is generated.
// To update, modify the specification and re-generate.
//
package examples.fileComment.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class PetType(
  @JsonValue
  public val `value`: String,
) {
  LEGGED("legged"),
  WINGED("winged"),
  FINNED("finned"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, PetType> = entries.associateBy(PetType::value)

    public fun fromValue(`value`: String): PetType? = mapping[value]
  }
}
