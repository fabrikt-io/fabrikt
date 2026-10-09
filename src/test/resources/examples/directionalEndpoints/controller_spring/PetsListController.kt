package examples.directionalEndpoints.controllers

import examples.directionalEndpoints.models.PetResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface PetsListController {
    /**
     *
     */
    @RequestMapping(
        value = ["/pets/list"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getPets(): ResponseEntity<List<PetResponse>>
}
