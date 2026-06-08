package examples.arrays.controllers

import examples.arrays.models.ArrayContainingComplexInlined
import examples.arrays.models.SomeEnum
import examples.arrays.models.Something
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.Valid
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface ItemsController {
    /**
     * Get items with a status filter
     * Retrieve a list of items filtered by their status.
     * @param category Filter items by category.
     * @param status Filter items by status.
     */
    @RequestMapping(
        value = ["/items"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @Valid @RequestParam(value = "category", required = false)
        category: List<SomeEnum>?,
        @Valid @RequestParam(value = "status", required = false)
        status: List<Something>?,
    ): ResponseEntity<List<ArrayContainingComplexInlined>>
}
