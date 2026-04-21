package examples.httpStatusCodeRangeDefinition.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import kotlin.Unit

@Controller
@Validated
@RequestMapping("")
public interface ExampleController {
    /**
     *
     */
    @RequestMapping(
        value = ["/example"],
        produces = [],
        method = [RequestMethod.GET],
    )
    public fun `get`(): ResponseEntity<Unit>

    /**
     *
     */
    @RequestMapping(
        value = ["/example"],
        produces = [],
        method = [RequestMethod.DELETE],
    )
    public fun deleteExample(): ResponseEntity<Unit>
}
