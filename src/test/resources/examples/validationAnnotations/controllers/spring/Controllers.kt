package examples.validationAnnotations.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.Size
import kotlin.Int
import kotlin.String
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface ExamplePath1Controller {
    /**
     * GET example path 1
     *
     * @param userName
     * @param friends
     * @param age
     * @param bio
     */
    @RequestMapping(
        value = ["/example-path-1"],
        produces = ["text/plain"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @RequestParam(value = "user_name", required = true) userName: String,
        @Valid @RequestParam(value = "friends", required = true) friends: List<String>,
        @Min(0) @Max(100) @RequestParam(value = "age", required = false) age: Int?,
        @Size(min = 20, max = 200) @RequestParam(value = "bio", required = false) bio: String?,
    ): ResponseEntity<String>
}
