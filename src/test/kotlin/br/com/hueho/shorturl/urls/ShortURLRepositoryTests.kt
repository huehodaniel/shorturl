package br.com.hueho.shorturl.urls

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShortURLRepositoryImplTests(
        @Autowired val repository: ShortURLRepositoryImpl,
        @Autowired val jdbc: JdbcTemplate
) {
    private val baseDataset: MutableMap<Long, String> = mutableMapOf()

    @BeforeAll
    fun `setup database fixtures`() {
        // Clean existing data
        jdbc.execute("TRUNCATE TABLE shorturls")

        val urls = arrayOf("http://example.com", "https://google.com", "https://uol.com.br")

        urls.forEach { url ->
            val keyHolder = GeneratedKeyHolder()
            jdbc.update({ con ->
                val stmt = con.prepareStatement("INSERT INTO shorturls(url) VALUES (?)", arrayOf("id"))
                stmt.setString(1, url)
                stmt
            }, keyHolder)

            val key = keyHolder.key!!.toLong()
            baseDataset[key] = url
        }
    }

    @AfterAll
    fun `tear down database fixtures`() = jdbc.execute("TRUNCATE TABLE shorturls")

    @Test
    fun `test findById`() {
        // Test for existing URLs
        baseDataset.forEach { id, url ->
            val shortURL = repository.findById(id)
            assertNotNull(shortURL)
            assertEquals(id, shortURL!!.id)
            assertEquals(url, shortURL!!.url)
        }

        // Test for missing id
        val noURL = repository.findById(-1)
        assertNull(noURL)
    }

    @Test
    fun `test saveURL`() {
        // Test for new URL
        val shortURL = repository.saveURL("https://shorturl.hueho.xyz")
        assertNotNull(shortURL.id)
        assertNotNull(shortURL.url)

        // Test for repeat URL
        val repeatURL = repository.saveURL("https://shorturl.hueho.xyz")
        assertEquals(shortURL, repeatURL)
    }

    @Test
    fun `test countVisitById`() {
        // Test for existing URL
        baseDataset.keys.forEach { id ->
            assertEquals(0, repository.findById(id)!!.visits)
            // Random visits
            val visits = (1..4).random()
            repeat(visits) { assertTrue(repository.countVisitById(id)) }
            // Check if matches
            assertEquals(visits.toLong(), repository.findById(id)!!.visits)
        }

        // Test for missing id
        val noURL = repository.countVisitById(-1)
        assertFalse(noURL)
    }
}

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShortURLRepositoryInMemoryImplTests() {
    private val repository = ShortURLRepositoryInMemoryImpl()

    @Test
    fun `sanity test`() {
        // Test for missing id
        val noURL = repository.findById(1)
        assertNull(noURL)

        // Test for saving new URL
        val shortURL = repository.saveURL("https://shorturl.hueho.xyz")
        assertNotNull(shortURL.id)
        assertNotNull(shortURL.url)

        val repeatURL = repository.saveURL("https://shorturl.hueho.xyz")
        assertEquals(shortURL, repeatURL)

        // Test for finding just added URL
        assertEquals(shortURL, repository.findById(shortURL.id))

        // Test count
        assertEquals(0L, shortURL.visits)
        assertTrue(repository.countVisitById(shortURL.id))
        val updatedURL = repository.findById(shortURL.id)!!
        assertEquals(1L, updatedURL.visits)
    }
}
