package br.com.hueho.shorturl.urls

import org.apache.commons.validator.routines.UrlValidator
import org.hashids.Hashids
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

/**
 * Defines a function that correcty creates a shortened full URL using a hashed ID
 */
typealias URLBuilder = (String) -> String

/**
 * Represents a shortened URL ready for consumption by external clients
 *
 * @property short the shortened URL
 * @property long the original full URL
 * @property visits the number of times the URL was visited
 */
data class ShortURLView(val id: String, val short: String, val long: String, val visits: Long) {
    companion object {
        fun from(original: ShortURL, encoder: Hashids, builder: URLBuilder) : ShortURLView {
            val stringId = encoder.encode(original.id)
            val shortUrl = builder(stringId)

            return ShortURLView(stringId, shortUrl, original.url, original.visits)
        }
    }
}

/** Error handling **/

/** Invalid URL **/
sealed class InvalidURLException(message: String) : RuntimeException(message)
class URLMaxSizeExceeded : InvalidURLException("Maximum size for URL is 1024 characters")
class InvalidURLFormat : InvalidURLException("Given URL does not conform to format")

/** URL not found **/
class EntityNotFound(type: String, id: Any) : RuntimeException("$type not found for id $id")

/**
 * Service class for retrieving and processing shortened URLs.
 */
interface ShortURLService {
    /**
     * Saves a new URL in the system. May do nothing if the URL already exists.
     *
     * @param url the URL to shorten
     * @param builder a function that builds the correct shortened URL
     * @throws [URLMaxSizeExceeded] if the URL exceeds the maximum allowed size
     * @throws [InvalidURLFormat] if the URL is in a invalid format
     */
    fun putNewURL(url: String, builder: URLBuilder) : ShortURLView

    /**
     * Retrieves URL info for a given shortened URL hashed id.
     *
     * @param hashedId the hashed ID to retrieve a URL for
     * @param builder a function that builds the correct shortened URL
     * @throws [EntityNotFound] if no URL exists for the given id
     */
    fun getURL(hashedId: String, builder: URLBuilder) : ShortURLView
}


/**
 * Default implementation for ShortURLService
 */
@Service
class ShortURLServiceImpl(
        @Qualifier("default") private val repo: ShortURLRepository,
        private val encoder: Hashids, private val validator: UrlValidator) :
    ShortURLService {
    override fun putNewURL(url: String, builder: URLBuilder) : ShortURLView {
        if(url.length > 1024) throw URLMaxSizeExceeded()
        if(!validator.isValid(url)) throw InvalidURLFormat()

        return ShortURLView.from(repo.saveURL(url), encoder, builder)
    }

    override fun getURL(hashedId: String, builder: URLBuilder) : ShortURLView {
        val longId = decode(hashedId)
        if(longId != null) {
            val result = repo.findById(longId)
            if(result != null) return ShortURLView.from(result, encoder, builder)
        }

        throw EntityNotFound("ShortURL", hashedId)
    }

    private fun decode(str: String) : Long? {
        val decoded = encoder.decode(str)
        return if(decoded.isEmpty()) null else decoded.first()
    }
}
