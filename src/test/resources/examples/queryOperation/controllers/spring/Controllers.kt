package examples.queryOperation.controllers

import examples.queryOperation.models.SearchRequest
import examples.queryOperation.models.SearchResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import javax.validation.Valid

@Controller
@Validated
@RequestMapping("")
public interface SearchController {
    /**
     *
     */
    @RequestMapping(
        value = ["/search"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getSearch(): ResponseEntity<SearchResult>

    /**
     *
     *
     * @param searchRequest
     */
    @RequestMapping(
        value = ["/search"],
        produces = ["application/json"],
        method = [RequestMethod.QUERY],
        consumes = ["application/json"],
    )
    public fun search(
        @RequestBody @Valid searchRequest: SearchRequest,
    ): ResponseEntity<SearchResult>
}
