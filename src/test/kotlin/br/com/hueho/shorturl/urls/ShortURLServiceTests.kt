package br.com.hueho.shorturl.urls

import org.apache.commons.validator.routines.UrlValidator
import org.hashids.Hashids
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShortURLServiceTests(
        @Autowired @Qualifier("inmemory") val repository: ShortURLRepository,
        @Autowired val encoder: Hashids,
        @Autowired validator: UrlValidator
) {
    private val service = ShortURLServiceImpl(repository, encoder, validator)
    private val baseDataset: MutableMap<String, ShortURLView> = mutableMapOf()

    @BeforeAll
    fun `setup repository fixtures`() {
        val urls = arrayOf("http://example.com", "https://google.com", "https://uol.com.br")

        urls.forEach { url ->
            val new = repository.saveURL(url)
            val hashedId = encoder.encode(new.id)
            baseDataset[hashedId] = ShortURLView(hashedId, mockURLBuilder(hashedId), url, 0)
        }
    }

    @Test
    fun `test getURL`() {
        // Test for existing URLs
        baseDataset.forEach { hashedId, expected ->
            val result = service.getURL(hashedId, this::mockURLBuilder)
            assertEquals(expected, result)
        }

        // Test for missing id
        assertThrows(EntityNotFound::class.java) {
            service.getURL("notfound", this::mockURLBuilder)
        }
    }

    @Test
    fun `test putNewURL`() {
        // Test for new URL (http)
        assertDoesNotThrow { service.putNewURL("http://shorturl.hueho.xyz", this::mockURLBuilder) }

        // Test for new URL (https)
        assertDoesNotThrow { service.putNewURL("https://shorturl.hueho.xyz", this::mockURLBuilder) }

        // Test for repeat URL
        assertDoesNotThrow { service.putNewURL("https://shorturl.hueho.xyz", this::mockURLBuilder) }

        // Test for invalid URL size
        assertThrows(URLMaxSizeExceeded::class.java) {
            service.putNewURL("X".repeat(1025), this::mockURLBuilder)
        }

        // Test for invalid URL format
        assertThrows(InvalidURLFormat::class.java) {
            service.putNewURL("X".repeat(1024), this::mockURLBuilder)
        }

        // Test for invalid URL scheme
        assertThrows(InvalidURLFormat::class.java) {
            service.putNewURL("ftp://shorturl.hueho.xyz", this::mockURLBuilder)
        }
    }

    private fun mockURLBuilder(id: String) = "https://test.com/$id"
}
