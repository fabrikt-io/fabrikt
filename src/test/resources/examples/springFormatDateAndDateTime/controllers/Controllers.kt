package ie.zalando.controllers

import org.springframework.format.`annotation`.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.Int
import kotlin.Unit

@Controller
@Validated
@RequestMapping("")
public interface ExampleController {
    /**
     *
     *
     * @param bDateTime
     * @param cInt
     * @param aDate
     */
    @RequestMapping(
        value = ["/example"],
        produces = [],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "bDateTime", required = true) bDateTime: OffsetDateTime,
        @RequestParam(value = "cInt", required = true) cInt: Int,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "aDate", required = false) aDate: LocalDate?,
    ): ResponseEntity<Unit>
}