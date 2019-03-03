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
    fun putNewURL(@RequestBody(required = false) url: String?) : ShortURLView {
        if(url != null) return service.putNewURL(url, ::generateURL)

        throw BodyNotSupplied()
    }

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

/** Error handling **/

@ControllerAdvice
class ShortURLErrorAdvice {
    /** Handling external exceptions **/

    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidURLHandler(ex: InvalidURLException) : JsonObject {
        return mapOf("error" to true, "message" to ex.message!!)
    }

    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun entityNotFoundHandler(ex: EntityNotFound) : JsonObject {
        return mapOf("error" to true, "message" to ex.message!!)
    }
}

/** Request errors **/

class BodyNotSupplied : RuntimeException("No URL supplied")

@ControllerAdvice
class BodyNotSuppliedAdvice {
    @ResponseBody
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun bodyNotSuppliedHandler(ex: BodyNotSupplied) : JsonObject {
        return mapOf("error" to true, "message" to ex.message!!)
    }
}
