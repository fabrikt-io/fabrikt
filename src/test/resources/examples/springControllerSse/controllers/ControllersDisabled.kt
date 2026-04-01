package examples.sseEmitter.controllers

import examples.sseEmitter.models.Event
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import kotlin.Any
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface InternalEventsStreamController {
    /**
     * Stream events for an entity
     *
     * @param entityId
     */
    @RequestMapping(
        value = ["/internal/events/{entity-id}/stream"],
        produces = ["text/event-stream", "application/problem+json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @PathVariable(value = "entity-id", required = true) entityId: Any,
    ): ResponseEntity<List<Event>>
}
