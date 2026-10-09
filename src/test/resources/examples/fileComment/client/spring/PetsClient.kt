//
// This file was generated from an OpenAPI specification by Fabrikt.
// DO NOT EDIT. Any changes will be overwritten the next time the code is generated.
// To update, modify the specification and re-generate.
//
package examples.fileComment.client

import examples.fileComment.models.Pet
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange

@Suppress("unused")
public interface PetsClient {
  /**
   * List all pets
   */
  @HttpExchange(
    url="/pets",
    method="GET",
    accept=["application/json"],
  )
  public fun listPets(@RequestHeader additionalHeaders: Map<String, Any> = emptyMap(), @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap()): List<Pet>
}
