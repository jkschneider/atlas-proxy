package com.netflix.atlas.proxy

import com.google.inject.AbstractModule
import com.netflix.atlas.config.ConfigManager
import com.netflix.iep.guice.GuiceHelper
import com.netflix.iep.service.ServiceManager
import com.netflix.spectator.api.Registry
import com.netflix.spectator.api.Spectator
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.io.File
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * @author Jon Schneider
 */
@SpringBootApplication
open class AtlasProxyApplication : SpringBootServletInitializer() {
    private val log = LoggerFactory.getLogger(AtlasProxyApplication::class.java)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(AtlasProxyApplication::class.java)
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

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Value("\${atlas.embedded:true}")
    private lateinit var atlasEmbedded: java.lang.Boolean
    private val guice = GuiceHelper()

    /**
     * Almost identical logic is found in atlas-standalone
     */
    @PostConstruct
    fun startEmbeddedAtlasIfNecessary() {
        if(!atlasEmbedded.booleanValue())
            return

        // Start an embedded Atlas server at a port governed by the provided Atlas config, or 7101 by default
        try {
            fun loadAdditionalConfigFiles(files: Array<String>) {
                files.forEach { f ->
                    log.info("loading config file: $f")
                    val file = File(f)
                    val c = if (file.exists())
                        ConfigFactory.parseFileAnySyntax(file)
                    else
                        ConfigFactory.parseResourcesAnySyntax(f)
                    ConfigManager.update(c)
                }
            }

            // if (args.nonEmpty) args else Array("static.conf")
            loadAdditionalConfigFiles(arrayOf("static.conf"))

            val modules = GuiceHelper.getModulesUsingServiceLoader()

            val configModule = object : AbstractModule() {
                override fun configure() {
                    bind(Registry::class.java).toInstance(Spectator.globalRegistry())
                    bind(Config::class.java).toInstance(ConfigManager.current())
                }
            }

            modules.add(configModule)

            guice.start(modules)

            // Ensure that service manager instance has been created
            guice.injector.getInstance(ServiceManager::class.java)

            guice.addShutdownHook()
        } catch(t: Throwable) {
            log.error("server failed to start, shutting down", t)
            System.exit(1)
        }
    }

    @PreDestroy
    fun stopAtlasIfNecessary() {
        guice.shutdown()
    }
}