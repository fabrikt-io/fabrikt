package com.cjbooms.fabrikt.servers.spring

import com.example.controllers.ContributorsController
import com.example.models.Contributor
import com.example.models.ContributorV1
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Compile-time proof for https://github.com/fabrikt-io/fabrikt/issues/840 (part B): the generated
 * [ContributorsController] must expose one method per versioned media type, with a typed return per version.
 * This override only compiles if `getContributorVersion1` returns `ContributorV1` and
 * `getContributorVersion2` returns `Contributor`.
 */
@RestController
open class ContributorControllerImpl : ContributorsController {
    override fun getContributorVersion1(id: String): ResponseEntity<ContributorV1> =
        ResponseEntity.ok(ContributorV1(id = id, name = "Ada"))

    override fun getContributorVersion2(id: String): ResponseEntity<Contributor> =
        ResponseEntity.ok(Contributor(id = id, name = "Ada", role = "author"))
}
