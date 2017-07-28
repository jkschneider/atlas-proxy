package io.pivotal.atlas.groovy.script

import com.fasterxml.jackson.databind.ObjectMapper
import io.pivotal.atlas.groovy.Tag
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL

class Select(private val atlasUri: String) {

    private val mapper = ObjectMapper()
    private val logger = LoggerFactory.getLogger(Select::class.java)

    fun timer(name: String, vararg tags: String): Timer {
        try {
            val resp = URL("$atlasUri/api/v1/tags/statistic?q=name,playback.startLatency,:eq").openStream().reader().readText()
            val stats = mapper.readValue(resp, Array<String>::class.java)
            if (!stats.contains("count") || !stats.contains("totalTime")) {
                throw InvalidTimeSeriesException(name, "'$name' is not a timer")
            }
        } catch(ignored: IOException) {
            logger.debug("Unable to validate that {} is a timer because the Atlas server is unavailable", name)
        }
        return Timer(name, Tag.zip(*tags))
    }

    fun counter(name: String, vararg tags: String): Counter {
        return Counter(name, Tag.zip(*tags))
    }
}
