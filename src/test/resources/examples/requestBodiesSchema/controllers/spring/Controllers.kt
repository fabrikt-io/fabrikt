package examples.requestBodiesSchema.controllers

import examples.requestBodiesSchema.models.CreateWidgetRequest
import examples.requestBodiesSchema.models.UpdateWidgetRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import javax.validation.Valid
import kotlin.String
import kotlin.Unit

@Controller
@Validated
@RequestMapping("")
public interface WidgetsController {
    /**
     * Create a new widget
     *
     * @param createWidgetRequest
     */
    @RequestMapping(
        value = ["/widgets"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun createWidget(
        @RequestBody @Valid createWidgetRequest: CreateWidgetRequest,
    ): ResponseEntity<Unit>

    /**
     * Update an existing widget
     *
     * @param updateWidgetRequest
     * @param id
     */
    @RequestMapping(
        value = ["/widgets/{id}"],
        produces = [],
        method = [RequestMethod.PUT],
        consumes = ["application/json"],
    )
    public fun updateWidget(
        @RequestBody @Valid updateWidgetRequest: UpdateWidgetRequest,
        @PathVariable(value = "id", required = true) id: String,
    ): ResponseEntity<Unit>
}
