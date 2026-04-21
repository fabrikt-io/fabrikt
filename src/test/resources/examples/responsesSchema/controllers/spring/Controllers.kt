package examples.responsesSchema.controllers

import examples.responsesSchema.models.SharedModel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod

@Controller
@Validated
@RequestMapping("")
public interface ThingsController {
    /**
     * Returns a thing using a ref-backed response — controller should use SharedModel
     */
    @RequestMapping(
        value = ["/things"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getThing(): ResponseEntity<SharedModel>
}
