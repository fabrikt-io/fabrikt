package examples.directionalEndpoints.controllers

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import examples.directionalEndpoints.models.PetStatusRequest
import examples.directionalEndpoints.models.StateRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam

@Controller
@Validated
@RequestMapping("")
public interface PetsController {
    /**
     *
     *
     * @param pet
     * @param status
     * @param state
     */
    @RequestMapping(
        value = ["/pets"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun createPet(
        @RequestBody pet: PetRequest,
        @RequestParam(value = "status", required = false) status: PetStatusRequest?,
        @RequestParam(value = "state", required = false) state: StateRequest?,
    ): ResponseEntity<PetResponse>
}
