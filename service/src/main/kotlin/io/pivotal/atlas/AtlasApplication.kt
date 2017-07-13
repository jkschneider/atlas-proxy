package io.pivotal.atlas

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

/**
 * @author Jon Schneider
 */
@SpringBootApplication
open class AtlasApplication : SpringBootServletInitializer() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(AtlasApplication::class.java)
        }
    }

    /**
    * Needed in development when running the UI independently of the service
    */
    @Bean
    open fun corsConfigurer(): WebMvcConfigurer = object : WebMvcConfigurerAdapter() {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/api/**")
        }
    }

    @Bean
    open fun restTemplate() = RestTemplate()
}