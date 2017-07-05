package com.netflix.atlas.proxy.script

import com.netflix.atlas.proxy.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

class Select(private val atlasUri: String,
             private val restTemplate: RestTemplate) {

    private val logger = LoggerFactory.getLogger(Select::class.java)

    fun timer(name: String, vararg tags: String): Timer {
        try {
            val stats = restTemplate.getForObject("$atlasUri/api/v1/tags/statistic?q=name,{metricName},:eq", Array<String>::class.java, name)
            if (!stats.contains("count") || !stats.contains("totalTime")) {
                throw InvalidTimeSeriesException(name, "'$name' is not a timer")
            }
        } catch(ignored: RestClientException) {
            logger.debug("Unable to validate that {} is a timer because the Atlas server is unavailable", name)
        }
        return Timer(name, Tag.zip(*tags))
    }

    fun counter(name: String, vararg tags: String): Counter {
        return Counter(name, Tag.zip(*tags))
    }
}
