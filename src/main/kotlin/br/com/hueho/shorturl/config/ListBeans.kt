package br.com.hueho.shorturl.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration

//@Configuration
class ListBeans {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun listApplicationBeans(ctx: ApplicationContext): CommandLineRunner {
        return CommandLineRunner {
            logger.info("Listing loaded beans")

            ctx.beanDefinitionNames.sortedArray().forEach { name ->
                logger.info("Bean loaded: $name")
            }
        }
    }
}