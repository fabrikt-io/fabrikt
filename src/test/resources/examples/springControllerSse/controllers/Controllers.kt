package examples.sseEmitter.controllers

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.servlet.mvc.method.`annotation`.SseEmitter
import kotlin.Any

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
    ): SseEmitter
}
