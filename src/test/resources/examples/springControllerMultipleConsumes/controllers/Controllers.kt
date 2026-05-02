package examples.sseEmitter.controllers

import examples.sseEmitter.models.UploadResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import javax.validation.Valid
import kotlin.ByteArray
import kotlin.String

@Controller
@Validated
@RequestMapping("")
public interface InternalUploadController {
    /**
     * Upload Images
     *
     * @param body image binary data
     * @param contentType mime type of the image, "image/png" or "image/jpeg"
     */
    @RequestMapping(
        value = ["/internal/upload"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["image/jpeg", "image/png", "application/octet-stream"],
    )
    public fun post(
        @RequestBody @Valid body: ByteArray,
        @RequestHeader(
            value = "Content-Type",
            required = true,
        ) contentType: String,
    ): ResponseEntity<UploadResponse>
}
