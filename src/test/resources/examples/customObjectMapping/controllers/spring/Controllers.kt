package examples.customObjectMapping.controllers

import examples.customObjectMapping.Money
import examples.customObjectMapping.models.Order
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
public interface OrdersController {
    /**
     *
     *
     * @param money
     */
    @RequestMapping(
        value = ["/orders"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun createOrder(
        @RequestBody @Valid money: Money,
    ): ResponseEntity<Order>
}
