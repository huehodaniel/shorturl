package br.com.hueho.shorturl.urls

import br.com.hueho.shorturl.common.JsonObject
import br.com.hueho.shorturl.frontend.FrontendController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder

@RestController
@RequestMapping("/api")
class ShortURLController(private val service: ShortURLService) {
    @GetMapping
    @ResponseBody
    fun greeting(): JsonObject {
        return mapOf("name" to "ShortURL", "version" to "0.1")
    }

    @PostMapping
    fun putNewURL(@RequestBody url: String) : ShortURLView = service.putNewURL(url, ::generateURL)

    @GetMapping("/{id}")
    fun getURL(@PathVariable id: String) : ShortURLView = service.getURL(id, ::generateURL)
}

/**
 * Default URL builder generation function
 */
fun generateURL(id: String) : String {
    return MvcUriComponentsBuilder
            .fromMethodName(FrontendController::class.java, "followURL", id)
            .build()
            .toUriString()
}

@ControllerAdvice
class InvalidURLAdvice {
    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidURLHandler(ex: InvalidURLException) : JsonObject {
        return mapOf("error" to true, "message" to ex.message!!)
    }
}

@ControllerAdvice
class EntityNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun entityNotFoundHandler(ex: EntityNotFound) : JsonObject {
        return mapOf("error" to true, "message" to ex.message!!)
    }
}
