package br.com.hueho.shorturl.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class LoadDatabase {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun initDatabase(jdbc: JdbcTemplate): CommandLineRunner {
        return CommandLineRunner { setup(jdbc) }
    }

    private fun setup(jdbc: JdbcTemplate) {
        // TODO: handle multiple databases
        logger.info("Setting up database")

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS shorturls (
                    id IDENTITY PRIMARY KEY,
                    url VARCHAR(1024) UNIQUE,
                    visits BIGINT DEFAULT 0
                )
            """.trimIndent())
    }
}