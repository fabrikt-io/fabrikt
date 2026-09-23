package com.example.controllers

import com.example.models.Pet
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import javax.`annotation`.processing.Generated
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public interface PetsController {
    /**
     * List all pets
     */
    @RequestMapping(
        value = ["/pets"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun listPets(): ResponseEntity<List<Pet>>
}
