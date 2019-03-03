package br.com.hueho.shorturl.frontend

import org.hashids.Hashids
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FrontendControllerTests(
        @Autowired private val mockMvc: MockMvc,
        @Autowired private val encoder: Hashids,
        @Autowired private val jdbc: JdbcTemplate) {

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
    fun `clean database after tests`() = jdbc.execute("TRUNCATE TABLE shorturls")

    @Test
    fun `test follow URL endpoint`() {
        // Test existing ids
        baseDataset.forEach  { id, url ->
            val hashedId = encoder.encode(id)

            mockMvc.perform(get("/ga/$hashedId"))
                    .andDo(print())
                    .andExpect(status().isFound)
                    .andExpect(redirectedUrl(url))
        }

        // Test missing id
        mockMvc.perform(get("/ga/nourl"))
                .andDo(print())
                .andExpect(status().isFound)
                .andExpect(redirectedUrl("/"))
    }
}