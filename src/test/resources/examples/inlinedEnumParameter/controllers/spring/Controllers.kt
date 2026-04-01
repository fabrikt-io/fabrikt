package examples.inlinedEnumParameter.controllers

import examples.inlinedEnumParameter.models.Categories
import examples.inlinedEnumParameter.models.DetailLevel
import examples.inlinedEnumParameter.models.Item
import examples.inlinedEnumParameter.models.SortOrder
import examples.inlinedEnumParameter.models.Status
import examples.inlinedEnumParameter.models.Tags
import examples.inlinedEnumParameter.models.XRequestSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.Valid
import kotlin.String
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface ItemsController {
    /**
     * List items
     *
     * @param sortOrder
     * @param status
     * @param xRequestSource
     */
    @RequestMapping(
        value = ["/items"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun listItems(
        @RequestParam(value = "sort_order", required = false) sortOrder: SortOrder?,
        @RequestParam(value = "status", required = false) status: Status?,
        @RequestHeader(value = "X-Request-Source", required = false) xRequestSource: XRequestSource?,
    ): ResponseEntity<List<Item>>

    /**
     * Get a single item
     *
     * @param itemId
     * @param detailLevel
     */
    @RequestMapping(
        value = ["/items/{itemId}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getItem(
        @PathVariable(value = "itemId", required = true) itemId: String,
        @RequestParam(value = "detail_level", required = false) detailLevel: DetailLevel?,
    ): ResponseEntity<Item>
}

@Controller
@Validated
@RequestMapping("")
public interface ItemsSearchController {
    /**
     * Search items with filters
     *
     * @param categories
     * @param tags
     */
    @RequestMapping(
        value = ["/items/search"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun searchItems(
        @Valid @RequestParam(value = "categories", required = true)
        categories: List<Categories>,
        @Valid @RequestParam(value = "tags", required = false)
        tags: List<Tags>?,
    ): ResponseEntity<List<Item>>
}
