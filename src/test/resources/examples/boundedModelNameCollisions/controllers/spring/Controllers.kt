package examples.boundedModelNameCollisions.controllers

import examples.boundedModelNameCollisions.models.Select
import examples.boundedModelNameCollisions.models.SelectExtra
import examples.boundedModelNameCollisions.models.SelectExtra2
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.Valid
import kotlin.Unit
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface AController {
    /**
     *
     *
     * @param select
     */
    @RequestMapping(
        value = ["/a"],
        produces = [],
        method = [RequestMethod.GET],
    )
    public fun getA(
        @Valid @RequestParam(value = "${'$'}select", required = false)
        select: List<Select>?,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface BController {
    /**
     *
     *
     * @param select
     */
    @RequestMapping(
        value = ["/b"],
        produces = [],
        method = [RequestMethod.GET],
    )
    public fun getB(
        @Valid @RequestParam(value = "${'$'}select", required = false)
        select: List<SelectExtra>?,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface CController {
    /**
     *
     *
     * @param select
     */
    @RequestMapping(
        value = ["/c"],
        produces = [],
        method = [RequestMethod.GET],
    )
    public fun getC(
        @Valid @RequestParam(value = "${'$'}select", required = false)
        select: List<SelectExtra2>?,
    ): ResponseEntity<Unit>
}
