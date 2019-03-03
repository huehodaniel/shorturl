package br.com.hueho.shorturl.frontend

import br.com.hueho.shorturl.urls.ShortURLRepository
import org.hashids.Hashids
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.view.RedirectView

@Controller
class FrontendController(@Qualifier("default") private val repo: ShortURLRepository, private val encoder: Hashids) {

    @GetMapping("/ga/{id}")
    @Transactional
    fun followURL(@PathVariable("id") id: String) : RedirectView {
        val longId = decode(id)
        val path = if(longId != null) {
            val result = repo.findById(longId)
            if(result != null) {
                repo.countVisitById(longId)
                result.url
            } else "/"
        } else "/"

        val redirect = RedirectView(path)
        redirect.setStatusCode(HttpStatus.FOUND)
        return redirect
    }

    private fun decode(str: String) : Long? {
        val decoded = encoder.decode(str)
        return if(decoded.isEmpty()) null else decoded.first()
    }

}