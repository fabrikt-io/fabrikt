package examples.modelSuffix.controllers

import examples.modelSuffix.models.ModeParameterDto
import examples.modelSuffix.models.RootTypeDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.Valid

@Controller
@Validated
@RequestMapping("")
public interface ExampleController {
    /**
     *
     *
     * @param mode
     */
    @RequestMapping(
        value = ["/example"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @RequestParam(value = "mode", required = true) mode: ModeParameterDto,
    ): ResponseEntity<RootTypeDto>

    /**
     *
     *
     * @param rootType
     */
    @RequestMapping(
        value = ["/example"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun post(
        @RequestBody @Valid rootType: RootTypeDto?,
    ): ResponseEntity<RootTypeDto>
}
