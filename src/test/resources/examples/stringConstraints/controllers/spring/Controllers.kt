package examples.stringConstraints.controllers

import examples.stringConstraints.FooId
import examples.stringConstraints.models.Bar
import org.springframework.format.`annotation`.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import java.time.OffsetDateTime
import javax.validation.constraints.Size
import kotlin.String

@Controller
@Validated
@RequestMapping("")
public interface FoosBarsController {
    /**
     *
     *
     * @param fooId
     * @param otherFooId
     * @param reference
     * @param createdAfter
     */
    @RequestMapping(
        value = ["/foos/{foo_id}/bars"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun listBars(
        @PathVariable(value = "foo_id", required = true) fooId: FooId,
        @RequestParam(value = "other_foo_id", required = false) otherFooId: FooId?,
        @Size(min = 1, max = 16) @RequestParam(value = "reference", required = false) reference: String?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "created_after", required = false) createdAfter:
            OffsetDateTime?,
    ): ResponseEntity<Bar>
}
