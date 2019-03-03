package br.com.hueho.shorturl.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class LoadDatabase {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    @Profile("h2")
    fun initH2Database(jdbc: JdbcTemplate): CommandLineRunner {
        return CommandLineRunner {
            logger.info("Setting up H2 database")

            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS shorturls (
                    id IDENTITY PRIMARY KEY,
                    url VARCHAR(1024) UNIQUE,
                    visits BIGINT DEFAULT 0
                )
            """.trimIndent())
        }
    }

    @Bean
    @Profile("postgres")
    fun initPostgresDatabase(jdbc: JdbcTemplate): CommandLineRunner {
        return CommandLineRunner {
            logger.info("Setting up PostgreSQL database")

            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS shorturls (
                    id BIGSERIAL PRIMARY KEY,
                    url VARCHAR(1024) UNIQUE,
                    visits BIGINT DEFAULT 0
                )
            """.trimIndent())
        }
    }
}
