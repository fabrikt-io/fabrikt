package examples.unsupportedInlinedDefinitions.controllers

import examples.unsupportedInlinedDefinitions.models.GetExampleResponseItem
import examples.unsupportedInlinedDefinitions.models.InlineEnum
import examples.unsupportedInlinedDefinitions.models.InlineObj
import examples.unsupportedInlinedDefinitions.models.PostExampleRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.Valid
import kotlin.Unit
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface ExampleController {
    /**
     *
     *
     * @param inlineEnum
     * @param inlineObj
     */
    @RequestMapping(
        value = ["/example"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @RequestParam(value = "inline_enum.", required = false) inlineEnum: InlineEnum?,
        @Valid @RequestParam(value = "inline_obj.", required = false) inlineObj: InlineObj?,
    ): ResponseEntity<List<GetExampleResponseItem>>

    /**
     *
     *
     * @param requestBody
     * @param inlineEnum
     */
    @RequestMapping(
        value = ["/example"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun post(
        @RequestBody @Valid requestBody: PostExampleRequest,
        @RequestParam(value = "inline_enum.", required = false) inlineEnum: InlineEnum?,
    ): ResponseEntity<Unit>
}
