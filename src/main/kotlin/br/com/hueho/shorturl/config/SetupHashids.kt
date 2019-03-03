package br.com.hueho.shorturl.config

import org.hashids.Hashids
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SetupHashids {

    @Value("\${shorturl.hashids.salt}")
    private lateinit var salt: String

    @Bean
    fun defaultHashidsInstance() : Hashids {
        return Hashids(salt, 8)
    }
}