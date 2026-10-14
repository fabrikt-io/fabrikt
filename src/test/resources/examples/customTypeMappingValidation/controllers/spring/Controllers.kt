package examples.customTypeMappingValidation.controllers

import examples.customTypeMappingValidation.MerchantId
import examples.customTypeMappingValidation.models.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.constraints.Size
import kotlin.String

@Controller
@Validated
@RequestMapping("")
public interface MerchantsOrdersController {
    /**
     *
     *
     * @param merchantId
     * @param referrerId
     * @param reference
     * @param slug
     */
    @RequestMapping(
        value = ["/merchants/{merchant_id}/orders"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun listOrders(
        @PathVariable(value = "merchant_id", required = true) merchantId: MerchantId,
        @RequestParam(value = "referrer_id", required = false) referrerId: MerchantId?,
        @Size(min = 1, max = 16) @RequestParam(value = "reference", required = false)
        reference: String?,
        @Size(max = 32) @RequestParam(value = "slug", required = false) slug: String?,
    ): ResponseEntity<Order>
}
