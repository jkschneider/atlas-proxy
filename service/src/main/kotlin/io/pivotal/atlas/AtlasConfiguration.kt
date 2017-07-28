package io.pivotal.atlas

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
import org.springframework.context.annotation.Configuration
import java.io.File
import javax.annotation.PreDestroy

@Configuration
open class AtlasConfiguration(@Value("\${atlas.db}") db: String) {
    private val log = LoggerFactory.getLogger(AtlasConfiguration::class.java)
    private val guice = GuiceHelper()

    /**
     * Almost identical logic is found in atlas-standalone
     */
    init {
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

            val conf = if (db == "static") "static.conf" else "memory.conf"
            loadAdditionalConfigFiles(arrayOf(conf))

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