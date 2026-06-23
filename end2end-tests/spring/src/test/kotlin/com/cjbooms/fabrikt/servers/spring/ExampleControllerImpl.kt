package com.cjbooms.fabrikt.servers.spring

import com.example.controllers.ExampleController
import com.example.models.ParameterEcho
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Regression coverage for https://github.com/fabrikt-io/fabrikt/issues/612
 *
 * This override only compiles if the generated [ExampleController.getExample] declares `param1` and
 * `limit` as non-nullable. Both are `required: false` but carry a `default`, so Spring always
 * populates them via `@RequestHeader`/`@RequestParam(defaultValue = ...)`. If the fix regressed and
 * those parameters were generated as `String?` / `Int?`, this override signature would no longer
 * match and the module would fail to compile.
 */
@RestController
open class ExampleControllerImpl : ExampleController {
    override fun getExample(
        param1: String,
        limit: Int,
        filter: String?,
    ): ResponseEntity<ParameterEcho> =
        ResponseEntity.ok(ParameterEcho(param1 = param1, limit = limit, filter = filter))
}
