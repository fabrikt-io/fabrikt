package examples.jakartaValidationAnnotations.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import kotlin.Long
import kotlin.Unit

@Controller
@Validated
@RequestMapping("")
public interface MaximumTestController {
    /**
     *
     *
     * @param pathId
     * @param headerid
     * @param queryid
     */
    @RequestMapping(
        value = ["/maximumTest/{pathId}"],
        produces = [],
        method = [RequestMethod.GET],
    )
    public fun getById(
        @Min(0) @Max(4_294_967_295) @PathVariable(value = "pathId", required = false) pathId: Long?,
        @Min(0) @Max(4_294_967_295) @RequestHeader(value = "headerid", required = false) headerid: Long?,
        @Min(0) @Max(4_294_967_295) @RequestParam(value = "queryid", required = false) queryid: Long?,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface MinimumTestController {
    /**
     *
     *
     * @param pathId
     * @param headerid
     * @param queryid
     */
    @RequestMapping(
        value = ["/minimumTest/{pathId}"],
        produces = [],
        method = [RequestMethod.GET],
    )
    public fun getById(
        @Min(-4_294_967_295) @PathVariable(value = "pathId", required = false) pathId: Long?,
        @Min(-4_294_967_295) @RequestHeader(value = "headerid", required = false) headerid: Long?,
        @Min(-4_294_967_295) @RequestParam(value = "queryid", required = false) queryid: Long?,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface MinMaxTestController {
    /**
     *
     *
     * @param pathId
     * @param headerid
     * @param queryid
     */
    @RequestMapping(
        value = ["/minMaxTest/{pathId}"],
        produces = [],
        method = [RequestMethod.GET],
    )
    public fun getById(
        @Min(-4_294_967_295) @Max(4_294_967_296) @PathVariable(value = "pathId", required = false) pathId: Long?,
        @Min(-4_294_967_295) @Max(4_294_967_296) @RequestHeader(value = "headerid", required = false) headerid: Long?,
        @Min(-4_294_967_295) @Max(4_294_967_296) @RequestParam(value = "queryid", required = false) queryid: Long?,
    ): ResponseEntity<Unit>
}
