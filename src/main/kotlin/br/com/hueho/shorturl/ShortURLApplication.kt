package br.com.hueho.shorturl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class ShortURLApplication

fun main(args: Array<String>) {
	runApplication<ShortURLApplication>(*args)
}
