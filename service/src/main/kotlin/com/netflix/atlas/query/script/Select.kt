package com.netflix.atlas.query.script

import com.netflix.atlas.query.Tag
import org.springframework.web.client.RestTemplate

class Select(private val atlasUri: String,
             private val restTemplate: RestTemplate) {

    fun timer(name: String, vararg tags: String): Timer {
        val stats = restTemplate.getForObject("$atlasUri/api/v1/tags/statistic?q=name,{metricName},:eq", Array<String>::class.java, name)
        if(!stats.contains("count") || !stats.contains("totalTime")) {
            throw InvalidTimeSeriesException(name, "'$name' is not a timer")
        }
        return Timer(name, Tag.zip(*tags))
    }

    fun counter(name: String, vararg tags: String): Counter {
        return Counter(name, Tag.zip(*tags))
    }
}
