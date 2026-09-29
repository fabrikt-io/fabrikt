package examples.directionalEndpoints.controllers

import examples.directionalEndpoints.models.PetResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import kotlin.String
import kotlin.collections.Map

@Controller
@Validated
@RequestMapping("")
public interface PetsIndexController {
    /**
     *
     */
    @RequestMapping(
        value = ["/pets/index"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getPetIndex(): ResponseEntity<Map<String, PetResponse?>>
}
