package examples.directionalEndpoints.controllers

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RequestPart
import kotlin.String

@Controller
@Validated
@RequestMapping("")
public interface PetsUploadController {
    /**
     *
     *
     * @param pet
     * @param token
     */
    @RequestMapping(
        value = ["/pets/upload"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["multipart/form-data"],
    )
    public fun uploadPet(
        @RequestPart(value = "pet", required = true) pet: PetRequest,
        @RequestParam(value = "token", required = true) token: String,
    ): ResponseEntity<PetResponse>
}
