package br.com.hueho.shorturl.urls

import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.skyscreamer.jsonassert.JSONParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShortURLControllerTests(
        @Autowired private val mockMvc: MockMvc,
        @Autowired private val service: ShortURLService,
        @Autowired val jdbc: JdbcTemplate) {

    @BeforeAll
    @AfterAll
    fun `clean database before and after tests`() = jdbc.execute("TRUNCATE TABLE shorturls")

    @Test
    fun `test greeting endpoint`() {
        mockMvc.perform(get("/api"))
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json("""
                {
                    "name": "ShortURL",
                    "version": "0.1"
                }
            """.trimIndent()))
    }

    @Test
    fun `test get URL`() {
        val newURL = service.putNewURL("http://unique.com.br", ::generateURL)

        mockMvc.perform(get("/api/${newURL.id}"))
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json("""
                    {
                        "id": "${newURL.id}",
                        "short": "${newURL.short}",
                        "long": "${newURL.long}",
                        "visits": ${newURL.visits}
                    }
                """.trimIndent()))
    }

    @Test
    fun `test plain put new URL endpoint`() {
        // Do initial request
        val result = mockMvc.perform(
                post("/api")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("http://example.com"))
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()

        val json = parseJSON(result.response.contentAsString)
        val id = json.getString("id")
        val short = json.getString("short")
        val long = json.getString("long")
        val visits = json.getLong("visits")

        assertEquals(generateURL(id), short)
        assertEquals("http://example.com", long)
        assertEquals(0L, visits)

        // Assure that new URL was inserted
        mockMvc.perform(get("/api/$id"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json("""
                    {
                        "id": "$id",
                        "short": "$short",
                        "long": "$long",
                        "visits": $visits
                    }
                """.trimIndent()))

        // Test repeat
        mockMvc.perform(
                post("/api")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("http://example.com"))
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo {
                    assertEquals(id, parseJSON(it.response.contentAsString).getString("id"))
                }
    }

    @Test
    fun `test put too big URL`() {
        mockMvc.perform(
                post("/api")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("X".repeat(1025)))
                .andDo(print())
                .andExpect(status().`is`(HttpStatus.BAD_REQUEST.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json("""
                    {
                        "error": true,
                        "message": "Maximum size for URL is 1024 characters"
                    }
                """.trimIndent()))
    }

    @Test
    fun `test invalid URL`() {
        mockMvc.perform(
                post("/api")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("X".repeat(1024)))
                .andDo(print())
                .andExpect(status().`is`(HttpStatus.BAD_REQUEST.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json("""
                    {
                        "error": true,
                        "message": "Given URL does not conform to format"
                    }
                """.trimIndent()))
    }

    @Test
    fun `test no URL`() {
        mockMvc.perform(
                post("/api").contentType(MediaType.TEXT_PLAIN))
                .andDo(print())
                .andExpect(status().`is`(HttpStatus.BAD_REQUEST.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json("""
                    {
                        "error": true,
                        "message": "No URL supplied"
                    }
                """.trimIndent()))
    }

    private fun parseJSON(str: String) : JSONObject = JSONParser.parseJSON(str) as JSONObject
}