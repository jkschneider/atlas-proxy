package io.pivotal.atlas

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletRegistration
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


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

@Controller
class ExceptionHandlingController {
    @RequestMapping("/api/v1/**")
    fun handle404(req: HttpServletRequest, resp: HttpServletResponse) {
        val url = URL("http://localhost:7101${req.requestURI}")
        val connection = url.openConnection() as HttpURLConnection
        req.headerNames.toList().forEach { header ->
            connection.setRequestProperty(header, req.getHeader(header))
        }
        connection.requestMethod = req.method
        connection.doOutput = true
        req.inputStream.copyTo(connection.outputStream)
        connection.connect()

        val responseCode = connection.responseCode
        if(responseCode == 200)
            connection.inputStream.copyTo(resp.outputStream)

        resp.status = responseCode
        println(req.toString())
    }
}