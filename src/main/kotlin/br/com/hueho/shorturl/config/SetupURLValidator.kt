package br.com.hueho.shorturl.config

import org.apache.commons.validator.routines.UrlValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SetupURLValidator {

    @Bean
    fun defaultURLValidator() : UrlValidator {
        return UrlValidator(arrayOf("http", "https"))
    }
}
