package io.pivotal.atlas

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


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
class AtlasProxy {
    @RequestMapping("/api/v1/**")
    fun proxyAtlas(req: HttpServletRequest, resp: HttpServletResponse) {
        println("Proxying: ${req.requestURI}")

        var queryParams = req.parameterNames.toList().map { param ->
            val value = when(param) {
                "q", "tz" -> req.getParameter(param)
                else -> URLEncoder.encode(req.getParameter(param), "UTF-8")
            }
            "$param=$value"
        }.joinToString("&")

        if(queryParams.isNotEmpty())
            queryParams = "?$queryParams"

        val url = "http://localhost:7101${req.requestURI}$queryParams"
        println("Proxing response from: " + url)
        val connection = URL(url).openConnection() as HttpURLConnection
        req.headerNames.toList().forEach { h ->
            connection.setRequestProperty(h, req.getHeader(h))
        }
        connection.requestMethod = req.method
        if(req.method == "POST") {
            connection.doOutput = true
            req.inputStream.copyTo(connection.outputStream)
        }
        connection.connect()

        val responseCode = connection.responseCode
        if(responseCode == 200)
            connection.inputStream.copyTo(resp.outputStream)

        connection.headerFields.toList().forEach { h ->
            for (v in h.second) {
                if(v != "chunked") { // don't want to deal with this complexity, we've just streamed the whole input stream to the response
                    resp.addHeader(h.first, v)
                }
            }
        }

        resp.status = responseCode
    }
}