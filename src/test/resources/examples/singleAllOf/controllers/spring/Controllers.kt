package examples.singleAllOf.controllers

import examples.singleAllOf.models.Result
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod

@Controller
@Validated
@RequestMapping("")
public interface TestController {
    /**
     *
     */
    @RequestMapping(
        value = ["/test"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun test(): ResponseEntity<Result?>
}
